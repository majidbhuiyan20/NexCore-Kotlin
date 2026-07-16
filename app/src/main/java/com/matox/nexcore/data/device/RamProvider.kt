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
 *  - Per-app memory ranking derived from the device-wide
 *    `runningAppProcesses` list (with `/proc` enumeration as a
 *    fallback) + [ActivityManager.getProcessMemoryInfo] for the
 *    currently-visible processes, plus each app's on-disk footprint
 *    (`sourceDir` + `dataDir`) as a stable proxy so background /
 *    sleeping apps also appear in the ranking.
 *
 * The instance holds the rolling history buffer (`ArrayDeque<Int>`)
 * so successive calls extend the timeline rather than re-creating it
 * on every poll. Failures are non-fatal: any sub-read that fails
 * returns zeros / nulls rather than throwing.
 */
class RamProvider(
    private val appContext: Context,
) {

    private val historyBuffer = ArrayDeque<Int>(HISTORY_CAPACITY + 1)
    @Volatile private var lastGood: RamSnapshot? = null

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

    // --- Top apps by memory footprint -------------------------------------

    /**
     * Returns the top [TOP_APPS_LIMIT] apps ranked by a hybrid memory
     * signal that survives Android's package-visibility sandbox:
     *
     *   - For packages whose processes we can see via
     *     [ActivityManager.runningAppProcesses] (foreground / visible
     *     / cached of our own app or apps we have access to), use the
     *     real PSS from [ActivityManager.getProcessMemoryInfo].
     *   - For every other installed app, use the sum of `sourceDir`
     *     + `dataDir` file sizes as a stable on-disk footprint proxy
     *     that's available without any runtime permission. This is
     *     the same proxy the App Manager's "Total Size" card uses.
     *
     * Why this hybrid:
     *   - `/proc/<pid>/smaps_rollup` reads return null for foreign
     *     processes on Android 11+ (SELinux `proc_mem` denial) — so
     *     a `/proc`-only path returns empty for user apps.
     *   - `ActivityManager.getProcessMemoryInfo` returns zero for
     *     foreign PIDs in `IMPORTANCE_CACHED` on most OEM ROMs.
     *   - Combining a real PSS for visible apps with an on-disk
     *     proxy for background apps gives the user a meaningful
     *     "Top memory consumers" list on every device.
     */
    private fun readTopApps(am: ActivityManager): List<AppRamUsage> {
        val pm = appContext.packageManager

        // 1. Index on-disk footprint by package — available for every
        //    installed app via PackageManager.
        val onDiskByPkg = indexOnDiskFootprint(pm)

        // 2. Build a PSS map keyed by package from ActivityManager
        //    (only the processes we can see get a non-zero entry).
        val pssByPkg = indexVisiblePss(am)

        // 3. Merge — prefer real PSS where available, otherwise use
        //    the on-disk proxy converted to MB.
        data class Aggregate(
            val packageName: String,
            val displayName: String,
            val isSystem: Boolean,
            var pssMb: Long,
            var hasRealPss: Boolean,
        )
        val byPkg = HashMap<String, Aggregate>()

        // Seed with on-disk footprints so background apps appear.
        for ((pkg, mb) in onDiskByPkg) {
            if (mb <= 0L) continue
            val info = runCatching { pm.getApplicationInfo(pkg, 0) }.getOrNull()
            val label = if (info != null) {
                runCatching { pm.getApplicationLabel(info).toString() }
                    .getOrDefault(pkg)
            } else pkg
            val isSystem = info?.let {
                (it.flags and ApplicationInfo.FLAG_SYSTEM) != 0
            } ?: false
            byPkg[pkg] = Aggregate(
                packageName = pkg,
                displayName = label,
                isSystem = isSystem,
                pssMb = mb,
                hasRealPss = false,
            )
        }

        // Overlay real PSS — wins when present.
        for ((pkg, realPssMb) in pssByPkg) {
            if (realPssMb <= 0L) continue
            val existing = byPkg[pkg]
            if (existing != null) {
                existing.pssMb = realPssMb
                existing.hasRealPss = true
            } else {
                val info = runCatching { pm.getApplicationInfo(pkg, 0) }.getOrNull()
                val label = if (info != null) {
                    runCatching { pm.getApplicationLabel(info).toString() }
                        .getOrDefault(pkg)
                } else pkg
                val isSystem = info?.let {
                    (it.flags and ApplicationInfo.FLAG_SYSTEM) != 0
                } ?: false
                byPkg[pkg] = Aggregate(
                    packageName = pkg,
                    displayName = label,
                    isSystem = isSystem,
                    pssMb = realPssMb,
                    hasRealPss = true,
                )
            }
        }

        return byPkg.values
            .sortedByDescending { it.pssMb }
            .take(TOP_APPS_LIMIT)
            .map {
                AppRamUsage(
                    packageName = it.packageName,
                    displayName = it.displayName,
                    pssMb = it.pssMb,
                    privateDirtyMb = if (it.hasRealPss) it.pssMb else 0L,
                    isSystem = it.isSystem,
                )
            }
    }

    /**
     * Walk every installed package and sum `sourceDir` (the APK) +
     * `dataDir` (the app's private data + cache). Returns a map of
     * `packageName → MB`. Pure filesystem read + PackageManager
     * metadata — no runtime permission needed.
     *
     * Excludes our own process so it doesn't appear in the list.
     */
    private fun indexOnDiskFootprint(pm: PackageManager): Map<String, Long> {
        val out = HashMap<String, Long>()
        val apps = runCatching {
            pm.getInstalledApplications(0)
        }.getOrNull() ?: return out
        val ourPkg = appContext.packageName
        for (info in apps) {
            val pkg = info.packageName
            if (pkg == ourPkg) continue
            var bytes = 0L
            info.sourceDir?.let { p ->
                runCatching { bytes += File(p).length() }
            }
            // dataDir = /data/data/<pkg> (or /data/user/0/<pkg>) —
            // includes the app's own files + cache.
            runCatching {
                val dataDir = info.dataDir
                if (!dataDir.isNullOrBlank()) {
                    val dir = File(dataDir)
                    if (dir.exists()) {
                        // Sum of every file under the data dir.
                        // walkTopDown caps recursion at any
                        // pathological depth but is fine for normal
                        // apps.
                        dir.walkTopDown().forEach { f ->
                            if (f.isFile) bytes += f.length()
                        }
                    }
                }
            }
            if (bytes > 0L) {
                out[pkg] = bytes / (1024L * 1024L)
            }
        }
        return out
    }

    /**
     * Build a per-package PSS map from [ActivityManager.runningAppProcesses]
     * + [ActivityManager.getProcessMemoryInfo]. Returns PSS in MB.
     * Apps that aren't currently running (and thus invisible to the
     * sandbox) get no entry — callers fall back to the on-disk proxy.
     */
    @Suppress("DEPRECATION")
    private fun indexVisiblePss(am: ActivityManager): Map<String, Long> {
        val processes = runCatching { am.runningAppProcesses }
            .getOrNull() ?: return emptyMap()
        if (processes.isEmpty()) return emptyMap()
        // Take every process we can see — even CACHED ones count.
        val pids = processes.map { it.pid }.toIntArray()
        val memInfos = runCatching { am.getProcessMemoryInfo(pids) }
            .getOrNull() ?: return emptyMap()
        val out = HashMap<String, Long>()
        for ((idx, mi) in memInfos.withIndex()) {
            val proc = processes.getOrNull(idx) ?: continue
            val pkg = proc.pkgList?.firstOrNull { it.isNotBlank() } ?: continue
            if (pkg == appContext.packageName) continue
            val pssKb = mi.totalPss.toLong()
            if (pssKb <= 0L) continue
            // Sum across packages if a process belongs to multiple.
            val pssMb = pssKb / 1024L
            out[pkg] = (out[pkg] ?: 0L) + pssMb
        }
        return out
    }

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
    }
}