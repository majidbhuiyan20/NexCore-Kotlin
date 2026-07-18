package com.matox.nexcore.data.device

import android.app.ActivityManager
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import com.matox.nexcore.domain.model.AppRamUsage
import com.matox.nexcore.domain.model.MemoryEvent
import com.matox.nexcore.domain.model.MemoryEventType
import com.matox.nexcore.domain.model.MetricAccent
import com.matox.nexcore.domain.model.RamSnapshot
import java.io.File
import java.io.RandomAccessFile
import java.util.UUID
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

    // --- Event synthesis state -------------------------------------------
    // Previous tick's RAM usage + lowMemory flag — used to detect
    // meaningful state changes (allocation, kernel reclaim, pressure
    // threshold crossing) and emit timeline events.
    private var previousUsedGb: Float = 0f
    private var previousLowMemory: Boolean = false
    private var previousPercent: Int = 0
    private val eventBuffer = ArrayDeque<MemoryEvent>(EVENT_CAPACITY + 1)
    // Cache of the previous tick's top-apps set, keyed by package.
    // Lets us emit APP_OPENED events for newly-visible apps.
    private var previousTopAppsByPkg: Map<String, Long> = emptyMap()

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

        // Synthesise timeline events from the delta between this tick
        // and the previous one. Skipped on the very first poll (when
        // previousUsedGb == 0 and previousTopAppsByPkg is empty) so
        // we don't fire a flood of synthetic events at startup.
        if (previousUsedGb > 0f || previousTopAppsByPkg.isNotEmpty()) {
            runCatching { synthesiseEvents(topApps, usedGb, pct, info.lowMemory) }
        }

        // Snapshot the new "previous" state for the next delta.
        previousUsedGb = usedGb
        previousLowMemory = info.lowMemory
        previousPercent = pct
        previousTopAppsByPkg = topApps.associate { it.packageName to it.pssMb }

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
            recentEvents = eventBuffer.toList().asReversed(),
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
        recentEvents = emptyList(),
    )

    // --- Event synthesis ---------------------------------------------------

    /**
     * Diff the current tick against the cached `previousXxx` fields
     * and append [MemoryEvent] entries to [eventBuffer] for any
     * meaningful state change. Synthesis rules:
     *
     *  - **APP_OPENED** — a package appeared in [topApps] that wasn't
     *    there last tick.
     *  - **LARGE_ALLOCATION** — `usedGb` jumped by > [LARGE_ALLOC_GB]
     *    between ticks (up only).
     *  - **LOW_MEMORY_WARNING** — `lowMemory` flipped false → true.
     *  - **BACKGROUND_CLEANUP** — `lowMemory` flipped true → false.
     *  - **MEMORY_PRESSURE_CHANGE** — `pct` crossed the 70 % line
     *    in either direction.
     *
     * Buffer is capped to [EVENT_CAPACITY]; oldest entries are dropped.
     * Synthesis never throws — `runCatching` wraps the call site.
     */
    private fun synthesiseEvents(
        topApps: List<AppRamUsage>,
        usedGb: Float,
        pct: Int,
        lowMemory: Boolean,
    ) {
        val now = System.currentTimeMillis()

        // 1. Newly-appearing top apps.
        val currentPkgs = topApps.map { it.packageName }.toSet()
        val newPkgs = currentPkgs - previousTopAppsByPkg.keys
        for (pkg in newPkgs) {
            val app = topApps.first { it.packageName == pkg }
            appendEvent(
                MemoryEvent(
                    id = UUID.randomUUID().toString(),
                    timestampMs = now,
                    type = MemoryEventType.APP_OPENED,
                    title = app.displayName,
                    subtitle = "Appeared in top memory consumers",
                    accent = MetricAccent.BLUE,
                )
            )
        }

        // 2. Large allocation — used jumped upward.
        val delta = usedGb - previousUsedGb
        if (delta >= LARGE_ALLOC_GB) {
            appendEvent(
                MemoryEvent(
                    id = UUID.randomUUID().toString(),
                    timestampMs = now,
                    type = MemoryEventType.LARGE_ALLOCATION,
                    title = "Large memory allocation",
                    subtitle = "RAM usage increased by ${formatGb(delta)}",
                    accent = MetricAccent.RED,
                )
            )
        } else if (delta <= -LARGE_ALLOC_GB) {
            // Big drop — heuristic: kernel reclaimed memory.
            appendEvent(
                MemoryEvent(
                    id = UUID.randomUUID().toString(),
                    timestampMs = now,
                    type = MemoryEventType.BACKGROUND_CLEANUP,
                    title = "Background cleanup",
                    subtitle = "Freed approximately ${formatGb(-delta)}",
                    accent = MetricAccent.GREEN,
                )
            )
        }

        // 3. lowMemory flag flips.
        if (lowMemory && !previousLowMemory) {
            appendEvent(
                MemoryEvent(
                    id = UUID.randomUUID().toString(),
                    timestampMs = now,
                    type = MemoryEventType.LOW_MEMORY_WARNING,
                    title = "Low memory warning",
                    subtitle = "System reports lowMemory threshold reached",
                    accent = MetricAccent.RED,
                )
            )
        } else if (!lowMemory && previousLowMemory) {
            appendEvent(
                MemoryEvent(
                    id = UUID.randomUUID().toString(),
                    timestampMs = now,
                    type = MemoryEventType.BACKGROUND_CLEANUP,
                    title = "Memory pressure cleared",
                    subtitle = "Kernel reclaimed cached pages",
                    accent = MetricAccent.GREEN,
                )
            )
        }

        // 4. Pressure threshold (70%) crossing.
        val crossedUp = previousPercent < PRESSURE_PCT && pct >= PRESSURE_PCT
        val crossedDown = previousPercent >= PRESSURE_PCT && pct < PRESSURE_PCT
        if (crossedUp) {
            appendEvent(
                MemoryEvent(
                    id = UUID.randomUUID().toString(),
                    timestampMs = now,
                    type = MemoryEventType.MEMORY_PRESSURE_CHANGE,
                    title = "Memory pressure rising",
                    subtitle = "Crossed $PRESSURE_PCT% threshold ($pct%)",
                    accent = MetricAccent.ORANGE,
                )
            )
        } else if (crossedDown) {
            appendEvent(
                MemoryEvent(
                    id = UUID.randomUUID().toString(),
                    timestampMs = now,
                    type = MemoryEventType.MEMORY_PRESSURE_CHANGE,
                    title = "Memory pressure easing",
                    subtitle = "Dropped below $PRESSURE_PCT% threshold ($pct%)",
                    accent = MetricAccent.CYAN,
                )
            )
        }
    }

    private fun appendEvent(event: MemoryEvent) {
        if (eventBuffer.size >= EVENT_CAPACITY) {
            eventBuffer.removeFirst()
        }
        eventBuffer.addLast(event)
    }

    private fun formatGb(v: Float): String {
        if (v <= 0f) return "0.0 GB"
        val rounded = (v * 10f).toInt() / 10f
        return "$rounded GB"
    }

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

        // 1. Index on-disk footprint by package — via the TTL cache so
        //    every poll cycle after the first doesn't re-walk every
        //    dataDir tree (was the #1 cold-frame-stutter source).
        val (onDiskByPkg, labelsByPkg, isSystemByPkg) =
            InstalledAppsFootprintCache.footprint(appContext)

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
            byPkg[pkg] = Aggregate(
                packageName = pkg,
                displayName = labelsByPkg[pkg] ?: pkg,
                isSystem = isSystemByPkg[pkg] ?: false,
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
                    hasRealPss = it.hasRealPss,
                )
            }
    }

    /**
     * Walk every installed package and sum `sourceDir` (the APK) +
     * `dataDir` (the app's private data + cache). Returns a map of
     * `packageName → MB`. Pure filesystem read + PackageManager
     * metadata — no runtime permission needed.
     *
     * **Delegated to [InstalledAppsFootprintCache]** which caches the
     * result for 30 s, so the 3-second RAM poll doesn't re-walk every
     * dataDir tree on every tick.
     */
    private fun indexOnDiskFootprint(@Suppress("UNUSED_PARAMETER") pm: PackageManager): Map<String, Long> {
        return InstalledAppsFootprintCache.footprint(appContext).first
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
        // 60 minutes at 3 s polling cadence → 1200 samples. ~4.8 KB.
        private const val HISTORY_CAPACITY: Int = 1200
        private const val TOP_APPS_LIMIT: Int = 8
        private const val MEMINFO_LINE_CAP: Int = 64
        // Max number of synthesised timeline events retained.
        private const val EVENT_CAPACITY: Int = 20
        // Minimum upward jump in GB to count as a "large allocation".
        private const val LARGE_ALLOC_GB: Float = 0.5f
        // RAM percent at which we say the device is under pressure.
        private const val PRESSURE_PCT: Int = 70
    }
}