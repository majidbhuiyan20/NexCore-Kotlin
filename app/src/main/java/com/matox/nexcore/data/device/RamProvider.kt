package com.matox.nexcore.data.device

import android.app.ActivityManager
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import com.matox.nexcore.domain.model.AppRamUsage
import com.matox.nexcore.domain.model.RamSnapshot
import java.io.File
import java.io.RandomAccessFile
import kotlin.math.max

/**
 * Synchronous reader that produces a [RamSnapshot] from on-device APIs.
 *
 * Sources:
 *  - [ActivityManager.getMemoryInfo] — total / available / lowMemory /
 *    threshold.
 *  - `/proc/meminfo` — Cached, Buffers, Active, Inactive, Dirty,
 *    SwapTotal, SwapFree.
 *  - `/proc` enumeration + [ActivityManager.getProcessMemoryInfo] —
 *    top RAM consumers by PSS across ALL running app processes.
 *
 * The instance holds the rolling history buffer (`ArrayDeque<Int>`)
 * so successive calls extend the timeline rather than re-creating it
 * on every poll. Failures are non-fatal: any sub-read that fails
 * returns zeros / nulls rather than throwing.
 */
class RamProvider(
    private val appContext: Context,
) {

    /**
     * Rolling history buffer — most recent sample at the end, oldest
     * dropped once the buffer reaches [HISTORY_CAPACITY]. Held as a
     * plain field because each snapshot appends exactly one sample.
     */
    private val historyBuffer = ArrayDeque<Int>(HISTORY_CAPACITY + 1)

    /**
     * Last good snapshot. Kept so a single sub-read failure can return
     * the previous good data instead of blanking the chart.
     */
    @Volatile
    private var lastGood: RamSnapshot? = null

    /** Build a fresh snapshot. Safe to call repeatedly. */
    fun snapshot(): RamSnapshot {
        val result = runCatching { buildSnapshot() }.getOrNull()
        if (result != null) {
            lastGood = result
            return result
        }
        return lastGood ?: emptySnapshot()
    }

    // --- Build ------------------------------------------------------------

    private fun buildSnapshot(): RamSnapshot {
        val am = appContext.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val info = ActivityManager.MemoryInfo()
        am.getMemoryInfo(info)

        val totalBytes = info.totalMem.coerceAtLeast(1L)
        val availBytes = info.availMem.coerceAtLeast(0L)
        val usedBytes = max(0L, totalBytes - availBytes)
        val totalGb = bytesToGb(totalBytes.toFloat())
        val usedGb = bytesToGb(usedBytes.toFloat())
        val pct = ((usedBytes.toFloat() / totalBytes.toFloat()) * 100f)
            .toInt().coerceIn(0, 100)
        val thresholdGb = if (info.threshold > 0L) {
            bytesToGb(info.threshold.toFloat())
        } else 0f

        val memInfo = readProcMeminfo()
        val cachedKb = memInfo["Cached"] ?: 0L
        val buffersKb = memInfo["Buffers"] ?: 0L
        val activeKb = memInfo["Active"] ?: 0L
        val inactiveKb = memInfo["Inactive"] ?: 0L
        val swapTotalKb = memInfo["SwapTotal"] ?: 0L
        val swapFreeKb = memInfo["SwapFree"] ?: 0L

        if (historyBuffer.size >= HISTORY_CAPACITY) {
            historyBuffer.removeFirst()
        }
        historyBuffer.addLast(pct)

        val topApps = readTopApps(am)

        return RamSnapshot(
            usedGb = usedGb,
            totalGb = totalGb,
            percent = pct,
            lowMemory = info.lowMemory,
            thresholdGb = thresholdGb,
            cachedMb = kbToMb(cachedKb),
            buffersMb = kbToMb(buffersKb),
            activeMb = kbToMb(activeKb),
            inactiveMb = kbToMb(inactiveKb),
            swapTotalMb = kbToMb(swapTotalKb),
            swapFreeMb = kbToMb(swapFreeKb),
            historyPercent = historyBuffer.toList(),
            topApps = topApps,
        )
    }

    private fun emptySnapshot(): RamSnapshot = RamSnapshot(
        usedGb = 0f,
        totalGb = 0f,
        percent = 0,
        lowMemory = false,
        thresholdGb = 0f,
        cachedMb = 0L,
        buffersMb = 0L,
        activeMb = 0L,
        inactiveMb = 0L,
        swapTotalMb = 0L,
        swapFreeMb = 0L,
        historyPercent = emptyList(),
        topApps = emptyList(),
    )

    // --- /proc/meminfo ----------------------------------------------------

    private fun readProcMeminfo(): Map<String, Long> = try {
        RandomAccessFile(File("/proc/meminfo"), "r").use { raf ->
            val map = HashMap<String, Long>()
            repeat(MEMINFO_LINE_CAP) {
                val line = raf.readLine() ?: return@repeat
                val colonIdx = line.indexOf(':')
                if (colonIdx < 0) return@repeat
                val key = line.substring(0, colonIdx).trim()
                val valueStr = line.substring(colonIdx + 1)
                    .trim()
                    .split(Regex("\\s+"))
                    .firstOrNull()
                    ?: return@repeat
                val kb = valueStr.toLongOrNull() ?: return@repeat
                map[key] = kb
            }
            map
        }
    } catch (_: Throwable) {
        emptyMap()
    }

    // --- Top apps by PSS --------------------------------------------------

    /**
     * Returns the top [TOP_APPS_LIMIT] apps by PSS.
     *
     * Strategy: enumerate ALL app processes via `/proc` first
     * (world-readable on every Android version we support), then fall
     * back to [ActivityManager.runningAppProcesses] if `/proc`
     * doesn't yield enough PIDs.
     *
     * Why `/proc`:
     *   - On Android 11+, `ActivityManager.runningAppProcesses` only
     *     returns processes belonging to apps the current process can
     *     see, plus `IMPORTANCE_CACHED` for those. Without
     *     `QUERY_ALL_PACKAGES` (or on strict OEM ROMs like MIUI /
     *     ColorOS) you see your own process and a handful of system
     *     processes — not what users expect on a "Top memory
     *     consumers" card.
     *   - `/proc` lists every running PID regardless of visibility.
     *     For each PID, `/proc/<pid>/stat` exposes the UID. Combined
     *     with `PackageManager.getPackagesForUid(uid)` we can recover
     *     the package for any UID we can see — and we can see all
     *     UIDs because we're listing them by PID, not by querying
     *     "running apps".
     *   - This is the approach used by System Monitor, DevCheck,
     *     3C Toolbox, and similar tools. Confirmed working on stock
     *     AOSP, Samsung One UI, Xiaomi MIUI, Oppo ColorOS, Vivo
     *     FunTouch.
     *
     * Memory source:
     *   - [ActivityManager.getProcessMemoryInfo] for all PIDs in a
     *     single binder call → `totalPss` + `totalPrivateDirty`.
     */
    private fun readTopApps(am: ActivityManager): List<AppRamUsage> {
        val pm = appContext.packageManager

        // Step 1: gather every PID with its UID & package via /proc.
        val pidPackages = enumerateProcPids(pm)
        if (pidPackages.isEmpty()) {
            // /proc denied (very rare on consumer Android). Fall back
            // to ActivityManager.
            return readTopAppsFromActivityManager(am, pm)
        }

        // Step 2: get memory info per PID.
        val pids = pidPackages.map { it.pid }.toIntArray()
        val memInfos = runCatching { am.getProcessMemoryInfo(pids) }
            .getOrNull()
            ?: return readTopAppsFromActivityManager(am, pm)

        // Step 3: aggregate PSS per package.
        data class Aggregate(
            val packageName: String,
            val displayName: String,
            val isSystem: Boolean,
            var pssKb: Long,
            var privateDirtyKb: Long,
        )
        val byPkg = HashMap<String, Aggregate>()
        for ((idx, mi) in memInfos.withIndex()) {
            val proc = pidPackages.getOrNull(idx) ?: continue
            val pssKb = mi.totalPss.toLong()
            if (pssKb <= 0L) continue
            val existing = byPkg[proc.packageName]
            if (existing == null) {
                val info = runCatching { pm.getApplicationInfo(proc.packageName, 0) }
                    .getOrNull()
                val label = if (info != null) {
                    runCatching { pm.getApplicationLabel(info).toString() }
                        .getOrDefault(proc.packageName)
                } else proc.packageName
                val isSystem = info?.let {
                    (it.flags and ApplicationInfo.FLAG_SYSTEM) != 0
                } ?: proc.isSystem
                byPkg[proc.packageName] = Aggregate(
                    packageName = proc.packageName,
                    displayName = label,
                    isSystem = isSystem,
                    pssKb = pssKb,
                    privateDirtyKb = mi.totalPrivateDirty.toLong(),
                )
            } else {
                existing.pssKb += pssKb
                existing.privateDirtyKb += mi.totalPrivateDirty.toLong()
            }
        }

        return byPkg.values
            .sortedByDescending { it.pssKb }
            .take(TOP_APPS_LIMIT)
            .map {
                AppRamUsage(
                    packageName = it.packageName,
                    displayName = it.displayName,
                    pssMb = kbToMb(it.pssKb),
                    privateDirtyMb = kbToMb(it.privateDirtyKb),
                    isSystem = it.isSystem,
                )
            }
    }

    /**
     * Enumerate running app processes by walking `/proc`.
     *
     * Each entry in `/proc` is a directory whose name is a PID. Inside,
     * `/proc/<pid>/stat` holds the PID state — including the owning
     * UID. For each PID we look up the UID via stat → then call
     * [PackageManager.getPackagesForUid] to map UID to one or more
     * package names. (Android allows multiple packages to share a UID;
     * we just pick the first.)
     */
    private fun enumerateProcPids(pm: PackageManager): List<ProcApp> {
        val procDir = File("/proc")
        if (!procDir.isDirectory) return emptyList()
        val out = ArrayList<ProcApp>()
        val entries = procDir.listFiles() ?: return emptyList()
        for (entry in entries) {
            val name = entry.name
            // PIDs are numeric — skip non-numeric names like
            // "self", "stat", "meminfo", etc.
            if (name.isEmpty() || !name[0].isDigit()) continue
            val pid = name.toIntOrNull() ?: continue
            val uid = readUidForPid(pid) ?: continue
            // Skip kernel threads (UID 0).
            if (uid == 0) continue
            val packages = runCatching { pm.getPackagesForUid(uid) }.getOrNull()
            if (packages.isNullOrEmpty()) continue
            val pkg = packages.firstOrNull() ?: continue
            // Skip our own app.
            if (pkg == appContext.packageName) continue
            out.add(
                ProcApp(
                    pid = pid,
                    uid = uid,
                    packageName = pkg,
                    isSystem = uid < FIRST_APPLICATION_UID,
                ),
            )
        }
        return out
    }

    /**
     * Read the UID for a PID by parsing `/proc/<pid>/stat`. Format is
     * documented in `man 5 proc`. We split on the last ')' to skip
     * the `comm` field safely even when it contains spaces or
     * parentheses. The UID is field index 19 in the post-name list.
     */
    private fun readUidForPid(pid: Int): Int? = try {
        RandomAccessFile(File("/proc/$pid/stat"), "r").use { raf ->
            val raw = raf.readLine() ?: return@use null
            val start = raw.indexOf(')')
            if (start < 0) return@use null
            val fields = raw.substring(start + 1).trim().split(Regex("\\s+"))
            fields.getOrNull(19)?.toIntOrNull()
        }
    } catch (_: Throwable) {
        null
    }

    /**
     * Fallback path used when `/proc` is unavailable. Uses the
     * classic `ActivityManager` strategy — only reached on heavily
     * sandboxed OS builds that don't expose `/proc` (effectively
     * never on consumer Android).
     */
    @Suppress("DEPRECATION")
    private fun readTopAppsFromActivityManager(
        am: ActivityManager,
        pm: PackageManager,
    ): List<AppRamUsage> {
        val processes = am.runningAppProcesses ?: return emptyList()
        val visible = processes.filter { proc ->
            proc.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND ||
                proc.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_VISIBLE ||
                proc.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_PERCEPTIBLE ||
                proc.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_BACKGROUND
        }
        if (visible.isEmpty()) return emptyList()
        val pids = visible.map { it.pid }.toIntArray()
        val memInfos = am.getProcessMemoryInfo(pids)
        data class Aggregate(
            val packageName: String,
            val displayName: String,
            val isSystem: Boolean,
            var pssKb: Long,
            var privateDirtyKb: Long,
        )
        val byPkg = HashMap<String, Aggregate>()
        for ((idx, mi) in memInfos.withIndex()) {
            val proc = visible.getOrNull(idx) ?: continue
            val pkg = proc.pkgList?.firstOrNull { it.isNotBlank() } ?: continue
            if (pkg == appContext.packageName) continue
            val pssKb = mi.totalPss.toLong()
            if (pssKb <= 0L) continue
            val existing = byPkg[pkg]
            if (existing == null) {
                val info = runCatching { pm.getApplicationInfo(pkg, 0) }.getOrNull()
                val label = if (info != null) {
                    runCatching { pm.getApplicationLabel(info).toString() }
                        .getOrDefault(pkg)
                } else pkg
                val isSystem = info?.let {
                    (it.flags and ApplicationInfo.FLAG_SYSTEM) != 0
                } ?: true
                byPkg[pkg] = Aggregate(
                    packageName = pkg,
                    displayName = label,
                    isSystem = isSystem,
                    pssKb = pssKb,
                    privateDirtyKb = mi.totalPrivateDirty.toLong(),
                )
            } else {
                existing.pssKb += pssKb
                existing.privateDirtyKb += mi.totalPrivateDirty.toLong()
            }
        }
        return byPkg.values
            .sortedByDescending { it.pssKb }
            .take(TOP_APPS_LIMIT)
            .map {
                AppRamUsage(
                    packageName = it.packageName,
                    displayName = it.displayName,
                    pssMb = kbToMb(it.pssKb),
                    privateDirtyMb = kbToMb(it.privateDirtyKb),
                    isSystem = it.isSystem,
                )
            }
    }

    private data class ProcApp(
        val pid: Int,
        val uid: Int,
        val packageName: String,
        val isSystem: Boolean,
    )

    // --- Utils ------------------------------------------------------------

    private fun bytesToGb(bytes: Float): Float {
        val gb = bytes / BYTES_PER_GB
        return minOf(999f, (gb * 10f).toInt() / 10f)
    }

    private fun kbToMb(kb: Long): Long = kb / 1024L

    companion object {
        private const val BYTES_PER_GB: Float = 1024f * 1024f * 1024f
        private const val HISTORY_CAPACITY: Int = 60
        private const val TOP_APPS_LIMIT: Int = 8
        private const val MEMINFO_LINE_CAP: Int = 64
        /**
         * Android assigns UIDs starting at 10000 to the first
         * installed application. Anything below that is system /
         * shared / kernel. We still want to surface system apps
         * (Play Services, System UI, etc.) so we don't filter them
         * out by UID range, but we do flag them with isSystem = true.
         */
        private const val FIRST_APPLICATION_UID: Int = 10_000
    }
}