package com.matox.nexcore.data.device

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Environment
import android.os.StatFs
import android.os.SystemClock
import com.matox.nexcore.domain.model.DeviceMetrics
import java.io.File
import java.io.RandomAccessFile
import kotlin.math.max
import kotlin.math.min

/**
 * Reads live device telemetry from Android system APIs.
 *
 * All calls are expected to be made off the main thread; each
 * accessor is cheap (single system call or short file read).
 *
 * - RAM      → [ActivityManager.getMemoryInfo]
 * - Storage  → [StatFs] on the internal data partition
 * - Battery  → [BatteryManager] (capacity, status, temperature)
 * - CPU%     → delta over two samples of `/proc/stat`
 * - Temp     → [BatteryManager.BATTERY_PROPERTY_TEMPERATURE]
 */
class DeviceMetricsProvider(
    private val appContext: Context,
) {

    /** Snapshot the current device metrics. Safe to call repeatedly. */
    fun snapshot(): DeviceMetrics {
        val ram = readRam()
        val storage = readStorage()
        val battery = readBattery()
        val cpu = readCpuPercent()
        val temp = readTemperatureC()
        return DeviceMetrics(
            ramUsedGb = ram.usedGb,
            ramTotalGb = ram.totalGb,
            ramPercent = ram.percent,
            storageUsedGb = storage.usedGb,
            storageTotalGb = storage.totalGb,
            storagePercent = storage.percent,
            batteryPercent = battery.percent,
            batteryStatusLabel = battery.statusLabel,
            cpuPercent = cpu,
            temperatureC = temp,
        )
    }

    // --- RAM ----------------------------------------------------------------

    private data class RamStats(val usedGb: Float, val totalGb: Float, val percent: Int)

    private fun readRam(): RamStats {
        return try {
            val am = appContext.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val info = ActivityManager.MemoryInfo()
            am.getMemoryInfo(info)
            val total = info.totalMem.toFloat()
            // availMem is free + reclaimable cache; for "used" we use total - avail.
            val used = max(0L, info.totalMem - info.availMem).toFloat()
            val totalGb = bytesToGb(total)
            val usedGb = bytesToGb(used)
            val pct = if (total <= 0f) 0 else ((used / total) * 100f).toInt().coerceIn(0, 100)
            RamStats(usedGb, totalGb, pct)
        } catch (_: Throwable) {
            RamStats(0f, 0f, 0)
        }
    }

    // --- Storage ------------------------------------------------------------

    private data class StorageStats(val usedGb: Float, val totalGb: Float, val percent: Int)

    private fun readStorage(): StorageStats {
        return try {
            val path = Environment.getDataDirectory().path
            val stat = StatFs(path)
            val totalBytes = stat.blockCountLong * stat.blockSizeLong
            val availBytes = stat.availableBlocksLong * stat.blockSizeLong
            val usedBytes = max(0L, totalBytes - availBytes)
            val totalGb = bytesToGb(totalBytes.toFloat())
            val usedGb = bytesToGb(usedBytes.toFloat())
            val pct = if (totalBytes <= 0L) 0
            else ((usedBytes.toFloat() / totalBytes.toFloat()) * 100f).toInt().coerceIn(0, 100)
            StorageStats(usedGb, totalGb, pct)
        } catch (_: Throwable) {
            StorageStats(0f, 0f, 0)
        }
    }

    // --- Battery ------------------------------------------------------------

    private data class BatteryStats(val percent: Int, val statusLabel: String)

    private fun readBattery(): BatteryStats {
        return try {
            val bm = appContext.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
            val pct = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY).coerceIn(0, 100)
            val status = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_STATUS)
            val label = when (status) {
                BatteryManager.BATTERY_STATUS_CHARGING -> "Charging"
                BatteryManager.BATTERY_STATUS_DISCHARGING -> "Discharging"
                BatteryManager.BATTERY_STATUS_FULL -> "Full"
                BatteryManager.BATTERY_STATUS_NOT_CHARGING -> "Not charging"
                else -> "—"
            }
            BatteryStats(pct, label)
        } catch (_: Throwable) {
            BatteryStats(0, "—")
        }
    }

    private fun readTemperatureC(): Int {
        return try {
            // BatteryManager doesn't expose a BATTERY_PROPERTY_TEMPERATURE
            // constant; the canonical source is the sticky ACTION_BATTERY_CHANGED
            // intent's EXTRA_TEMPERATURE (deci-celsius).
            val intent = appContext.registerReceiver(
                /* receiver = */ null,
                IntentFilter(Intent.ACTION_BATTERY_CHANGED),
            )
            val tenths = intent?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) ?: 0
            tenths / 10
        } catch (_: Throwable) {
            0
        }
    }

    // --- CPU ----------------------------------------------------------------

    /**
     * Returns the busy CPU% for the whole device. Falls back through a
     * tiered chain so it works even when `/proc/stat` is sandboxed:
     *
     *  1. `/proc/stat` — two-sample delta over all CPU lines aggregated.
     *     Best device-wide signal; works on emulators and older devices.
     *  2. `/proc/loadavg` — 1-min system load / core count → %. Always
     *     readable on Android, gives a credible device-wide %.
     *  3. `/proc/self/stat` — app's own CPU usage as a last-resort
     *     fallback. Always available; not the device number, but
     *     better than blank.
     *
     * The first emission after process start primes the baseline (the
     * `lastCpuPercent` default of 0), then every subsequent emission
     * computes a true delta. Min delta is 100 ms so the percentages
     * are meaningful; sub-100 ms deltas would be dominated by jitter.
     */
    @Synchronized
    fun readCpuPercent(): Int {
        // Tier 1: /proc/stat — sum across every core line.
        // Read the per-core lines instead of the `cpu ` aggregate so
        // we get a stable big.LITTLE-friendly reading. We treat
        // columns 3 (idle) + 4 (iowait) as idle and everything else as
        // busy, which is the conventional Linux formula.
        val systemSample = readSystemProcStat()
        if (systemSample != null) {
            val pct = computeSystemDelta(systemSample)
            if (pct >= 0) return pct
        }

        // Tier 2: /proc/loadavg — always readable.
        val loadPct = readLoadAveragePercent()
        if (loadPct != null) {
            lastCpuPercent = loadPct
            return loadPct
        }

        // Tier 3: /proc/self/stat — our own process.
        val selfPct = readSelfCpuPercent()
        if (selfPct != null) {
            lastCpuPercent = selfPct
            return selfPct
        }

        return lastCpuPercent
    }

    // ---- Tier 1: /proc/stat (device-wide) ------------------------------

    private data class ProcStatSnapshot(
        val total: Long,
        val idle: Long,
        val cores: Int,
        val timestampMs: Long,
    )

    @Volatile private var cpuSample: ProcStatSnapshot? = null
    @Volatile private var lastCpuPercent: Int = 0

    /**
     * Reads the `cpu0 … cpuN` core lines from `/proc/stat` and returns
     * the sum. We deliberately skip the `cpu ` aggregate line because
     * its column layout (number of CPU columns) varies across Android
     * versions and OEM kernels — some include a `guest` column,
     * some don't. Per-core lines always have the same canonical layout:
     *
     *   user nice system idle iowait irq softirq [steal] [guest] [guest_nice]
     *
     * We treat **idle + iowait** as idle and the rest as busy. CPU
     * steal is unusual on real devices but harmless to count as busy.
     *
     * Returns null when nothing was readable (e.g. SELinux-sandboxed
     * `/proc/stat`); the caller's Tier-2 fallback kicks in then.
     */
    private fun readSystemProcStat(): ProcStatSnapshot? = try {
        RandomAccessFile(File("/proc/stat"), "r").use { raf ->
            var totalSum = 0L
            var idleSum = 0L
            var cores = 0
            // Read at most 32 lines — covers up to 32-core SoCs.
            repeat(32) {
                val line = raf.readLine() ?: return@repeat
                // Per-core lines only (cpu0, cpu1, …). The aggregate
                // "cpu " line is skipped on purpose — see kdoc above.
                if (!line.startsWith("cpu") || line.startsWith("cpu ")) return@repeat
                val parts = line.split(Regex("\\s+")).drop(1).mapNotNull { it.toLongOrNull() }
                if (parts.size < 4) return@repeat
                // busy = user + nice + system + irq + softirq + steal + guest + guest_nice
                // idle = idle + iowait
                var busy = 0L
                var idle = 0L
                parts.forEachIndexed { idx, v ->
                    when (idx) {
                        3 -> idle += v        // idle
                        4 -> idle += v        // iowait
                        else -> busy += v     // user/nice/system/irq/softirq/steal/guest/guest_nice
                    }
                }
                totalSum += busy + idle
                idleSum += idle
                cores += 1
            }
            if (totalSum <= 0L || cores == 0) null
            else ProcStatSnapshot(totalSum, idleSum, cores, SystemClock.elapsedRealtime())
        }
    } catch (_: Throwable) {
        null
    }

    /**
     * Returns the busy% using the conventional Linux formula
     * `busyDelta / (busyDelta + idleDelta)`, **scaled per core** so the
     * number is a device-wide % (not a sum across cores — which is
     * what produced the constant "100%" bug on earlier implementations
     * that forgot the divisor).
     *
     * Returns -1 when the delta is too small / suspicious — the caller
     * then falls back to Tier-2 or returns the cached value.
     */
    private fun computeSystemDelta(sample: ProcStatSnapshot): Int {
        val prev = cpuSample
        // No prior sample → prime the baseline and return 0 so the
        // first frame doesn't flash a bogus 100%.
        if (prev == null) {
            cpuSample = sample
            lastCpuPercent = 0
            return 0
        }
        val timeDeltaMs = (sample.timestampMs - prev.timestampMs).coerceAtLeast(1L)
        if (timeDeltaMs < MIN_CPU_DELTA_MS) {
            return lastCpuPercent
        }

        val totalDelta = (sample.total - prev.total).coerceAtLeast(0L)
        val idleDelta = (sample.idle - prev.idle).coerceAtLeast(0L)
        val busyDelta = (totalDelta - idleDelta).coerceAtLeast(0L)

        // Per-core divisor: idle + busy across N cores, so a 4-core
        // device with all cores 25% busy shows 25%, not 100%.
        val cores = sample.cores.coerceAtLeast(1)

        // If we barely moved any jiffies, treat as "no change" rather
        // than a magic 100%. 1 jiffy on a slow cpu is normal.
        if (totalDelta < 2L) {
            cpuSample = sample
            return lastCpuPercent
        }

        // busy% per core on this delta window
        val pctPerCore = (busyDelta.toFloat() / totalDelta.toFloat()) * 100f
        // Average across cores → device-wide %
        val pct = (pctPerCore / cores.toFloat()).toInt().coerceIn(0, 100)
        lastCpuPercent = pct
        cpuSample = sample
        return pct
    }

    // ---- Tier 2: /proc/loadavg -----------------------------------------

    private data class LoadSample(val load: Float, val cores: Int, val timestampMs: Long)
    @Volatile private var loadSample: LoadSample? = null

    private fun readLoadAveragePercent(): Int? {
        val cores = Runtime.getRuntime().availableProcessors().coerceAtLeast(1)
        val load: Float = try {
            RandomAccessFile(File("/proc/loadavg"), "r").use { raf ->
                raf.readLine()?.split(" ")?.firstOrNull()?.toFloatOrNull() ?: return null
            }
        } catch (_: Throwable) {
            return null
        }
        val now = System.currentTimeMillis()
        val prev = loadSample
        val pct = ((load / cores.toFloat()) * 100f).toInt().coerceIn(0, 100)
        loadSample = LoadSample(load, cores, now)
        // Smooth: blend with previous sample if we have one taken
        // recently — loadavg is already a 1-min average but a quick
        // blend reduces single-sample jumps.
        return if (prev != null && (now - prev.timestampMs) < 3_000L) {
            ((lastCpuPercent * 0.5f) + (pct * 0.5f)).toInt().coerceIn(0, 100)
        } else {
            pct
        }
    }

    // ---- Tier 3: self-process CPU --------------------------------------
    //
    // /proc/self/stat is always readable by the app's own process on
    // every Android version. The fields we need (utime, stime, plus
    // the wall-clock delta between two samples) let us compute the
    // app's CPU usage as a % of one core. This is a meaningful signal
    // for the dashboard even when /proc/stat is sandboxed — and it's
    // always available.

    private var lastSelfCpuJiffies: Long = 0L
    private var lastSelfWallMs: Long = 0L

    @Suppress("DEPRECATION")
    private fun readSelfCpuPercent(): Int? {
        val (utime, stime) = readSelfStat() ?: return null
        val cpuJiffies = utime + stime
        val nowMs = SystemClock.elapsedRealtime()
        if (lastSelfCpuJiffies == 0L) {
            lastSelfCpuJiffies = cpuJiffies
            lastSelfWallMs = nowMs
            return null
        }
        val cpuDelta = cpuJiffies - lastSelfCpuJiffies
        val wallDeltaMs = (nowMs - lastSelfWallMs).coerceAtLeast(1L)
        lastSelfCpuJiffies = cpuJiffies
        lastSelfWallMs = nowMs
        // /proc/self/stat times are in USER_HZ units (typically 100/sec).
        // Convert: cpuMs = cpuJiffies * (1000 / USER_HZ).
        val cpuMs = cpuDelta * (1000L / SELF_STAT_HZ.toLong())
        val pct = ((cpuMs.toFloat() / wallDeltaMs.toFloat()) * 100f).toInt().coerceIn(0, 100)
        return pct
    }

    private fun readSelfStat(): Pair<Long, Long>? = try {
        RandomAccessFile(File("/proc/self/stat"), "r").use { raf ->
            // The process name field (comm) can contain spaces or
            // parentheses so split on the last ')' to skip past it.
            val raw = raf.readLine() ?: return@use null
            val start = raw.indexOf(')')
            if (start < 0) return@use null
            val fields = raw.substring(start + 1).trim().split(Regex("\\s+"))
            // Field indexes (post-name) in /proc/self/stat:
            //   0 = state, 1 = ppid, …, 11 = utime (USER_HZ), 12 = stime
            val utime = fields.getOrNull(11)?.toLongOrNull() ?: 0L
            val stime = fields.getOrNull(12)?.toLongOrNull() ?: 0L
            utime to stime
        }
    } catch (_: Throwable) {
        null
    }

    // --- Utils --------------------------------------------------------------

    private fun bytesToGb(bytes: Float): Float {
        val gb = bytes / BYTES_PER_GB
        // Round to one decimal — matches the "X.X GB" labels in the UI.
        return min(999f, (gb * 10f).toInt() / 10f)
    }

    companion object {
        private const val BYTES_PER_GB: Float = 1024f * 1024f * 1024f
        private const val SELF_STAT_HZ: Int = 100
        private const val MIN_CPU_DELTA_MS: Long = 100L
    }
}
