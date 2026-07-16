package com.matox.nexcore.data.device

import android.annotation.SuppressLint
import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.os.Build
import android.os.SystemClock
import com.matox.nexcore.domain.model.CpuAppUsage
import com.matox.nexcore.domain.model.CpuSnapshot
import java.io.File
import java.io.RandomAccessFile

/**
 * Synchronous reader that produces a [CpuSnapshot] from on-device APIs.
 *
 * Sources:
 *  - [DeviceMetricsProvider.readCpuPercent] — device-wide % via
 *    `/proc/stat` (Tier 1), `/proc/loadavg` (Tier 2), `/proc/self/stat`
 *    (Tier 3 fallback). Injected rather than re-implemented.
 *  - `/sys/devices/system/cpu/cpu{0..N}/cpufreq/scaling_cur_freq` — current
 *    per-core frequency in kHz. We read up to [MAX_DISPLAY_CORES] cores
 *    for the UI; the actual [coreCount] comes from
 *    `Runtime.availableProcessors()`.
 *  - `Build.SOC_MODEL` (API 31+) with `Build.HARDWARE` fallback for
 *    the SoC label.
 *  - `SystemClock.elapsedRealtime()` for device uptime.
 *  - [UsageStatsManager.queryUsageStats] (with
 *    `PACKAGE_USAGE_STATS`) for the top apps by foreground CPU time.
 *
 * The provider holds a rolling buffer (`ArrayDeque<Int>`) of percent
 * samples so the chart animates without re-querying the OS.
 *
 * Failures are non-fatal: any sub-read that fails returns zeros / nulls
 * rather than throwing. The [snapshot] method caches the last good
 * snapshot and returns it on failure so the UI never blanks out.
 */
class CpuProvider(
    private val appContext: Context,
    private val deviceProvider: DeviceMetricsProvider,
) {

    private val historyBuffer = ArrayDeque<Int>(HISTORY_CAPACITY + 1)

    @Volatile private var lastGood: CpuSnapshot? = null

    fun snapshot(): CpuSnapshot {
        val result = runCatching { buildSnapshot() }.getOrNull()
        if (result != null) {
            lastGood = result
            return result
        }
        // On failure, return the cached snapshot so the UI keeps rendering.
        return lastGood ?: emptySnapshot()
    }

    private fun emptySnapshot(): CpuSnapshot = CpuSnapshot(
        overallPercent = 0,
        perCoreFrequenciesMhz = emptyList(),
        coreCount = Runtime.getRuntime().availableProcessors(),
        socModel = "",
        uptimeMs = SystemClock.elapsedRealtime(),
        historyPercent = emptyList(),
        topApps = emptyList(),
    )

    private fun buildSnapshot(): CpuSnapshot {
        // --- Overall % ---
        val pct = deviceProvider.readCpuPercent()

        // --- Per-core frequencies ---
        val cores = Runtime.getRuntime().availableProcessors().coerceAtLeast(1)
        val displayCores = minOf(cores, MAX_DISPLAY_CORES)
        val perCoreFreqs = List(displayCores) { idx -> readCoreFreqMhz(idx) }

        // --- SoC label ---
        val soc = detectSocModel()

        // --- Uptime ---
        val uptime = SystemClock.elapsedRealtime()

        // --- History buffer (1 sample per call) ---
        if (historyBuffer.size >= HISTORY_CAPACITY) historyBuffer.removeFirst()
        historyBuffer.addLast(pct)

        // --- Top apps ---
        val topApps = readTopApps()

        return CpuSnapshot(
            overallPercent = pct,
            perCoreFrequenciesMhz = perCoreFreqs,
            coreCount = cores,
            socModel = soc,
            uptimeMs = uptime,
            historyPercent = historyBuffer.toList(),
            topApps = topApps,
        )
    }

    /**
     * Reads a single core's current frequency in MHz.
     *
     * Returns `0` when:
     *  - the kernel exposes no `scaling_cur_freq` (some devices),
     *  - the file is unreadable due to SELinux,
     *  - the core is offline / parked.
     */
    private fun readCoreFreqMhz(core: Int): Int = try {
        val path = "/sys/devices/system/cpu/cpu$core/cpufreq/scaling_cur_freq"
        RandomAccessFile(File(path), "r").use { raf ->
            val raw = raf.readLine()?.trim().orEmpty()
            // /sys values are in kHz.
            val khz = raw.toLongOrNull() ?: return 0
            // Divide by 1000 → MHz. Clamp to <= 0 if the device reports
            // a nonsense number.
            val mhz = khz / 1000L
            when {
                mhz <= 0L -> 0
                mhz > Int.MAX_VALUE -> Int.MAX_VALUE
                else -> mhz.toInt()
            }
        }
    } catch (_: Throwable) {
        0
    }

    /** SOC_MODEL (API 31+) over HARDWARE. Returns an empty string on miss. */
    private fun detectSocModel(): String {
        val socModel = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            runCatching { Build.SOC_MODEL }.getOrNull().orEmpty()
        } else {
            ""
        }
        if (socModel.isNotBlank()) return socModel
        return runCatching { Build.HARDWARE }.getOrNull().orEmpty()
    }

    /**
     * Top apps by foreground time over the last 24 hours. Maps
     * `totalTimeInForeground` → a 0..100 share so the section renders
     * the same shape as the Battery top-apps card.
     *
     * Wrapped in `runCatching` — fails silently when the special
     * `PACKAGE_USAGE_STATS` permission has been revoked.
     */
    @SuppressLint("MissingPermission")
    private fun readTopApps(): List<CpuAppUsage> {
        val raw: List<Pair<String, Long>> = runCatching {
            val usm = appContext.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager
                ?: return@runCatching emptyList()
            val stats: List<UsageStats> = usm.queryUsageStats(
                UsageStatsManager.INTERVAL_DAILY,
                System.currentTimeMillis() - 24L * 60 * 60 * 1000,
                System.currentTimeMillis(),
            ) ?: emptyList()
            stats
                .filter { it.totalTimeInForeground > 0 }
                .sortedByDescending { it.totalTimeInForeground }
                .take(5)
                .map { it.packageName to it.totalTimeInForeground }
        }.getOrDefault(emptyList())

        if (raw.isEmpty()) return emptyList()

        val total = raw.sumOf { it.second }.coerceAtLeast(1L)
        return raw.map { (pkg, ms) ->
            val name = runCatching {
                val info = appContext.packageManager.getApplicationInfo(pkg, 0)
                appContext.packageManager.getApplicationLabel(info).toString()
            }.getOrDefault(pkg)
            val pct = ((ms.toFloat() / total.toFloat()) * 100f).coerceIn(0f, 100f)
            CpuAppUsage(
                packageName = pkg,
                displayName = name,
                estimatedPct = pct,
            )
        }
    }

    companion object {
        /** 180 samples = 3 minutes at 1 Hz. */
        private const val HISTORY_CAPACITY: Int = 180

        /** Capped at 8 cores for UI readability on large SoCs. */
        private const val MAX_DISPLAY_CORES: Int = 8
    }
}
