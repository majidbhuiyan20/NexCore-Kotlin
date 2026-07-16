package com.matox.nexcore.data.device

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Environment
import android.os.StatFs
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
     * Returns the busy CPU% by comparing two samples of `/proc/stat`
     * taken ~500 ms apart on the first call. Subsequent calls reuse
     * the previous sample so callers can poll without blocking.
     */
    @Synchronized
    fun readCpuPercent(): Int {
        val now = System.currentTimeMillis()
        val first = cpuSample
        val needsFirst = first == null || (now - first.timestampMs) > 5_000L
        val sample = readProcStatSnapshot() ?: return lastCpuPercent
        if (needsFirst) {
            cpuSample = CpuSample(sample, now)
            // Return the last computed value rather than 0 to avoid flicker.
            return lastCpuPercent
        }
        val prev = first!!
        val totalDelta = (sample.total - prev.snapshot.total).coerceAtLeast(1L)
        val idleDelta = (sample.idle - prev.snapshot.idle).coerceAtLeast(0L)
        val busy = totalDelta - idleDelta
        val pct = ((busy.toFloat() / totalDelta.toFloat()) * 100f).toInt().coerceIn(0, 100)
        lastCpuPercent = pct
        cpuSample = CpuSample(sample, now)
        return pct
    }

    private data class ProcStatSnapshot(val total: Long, val idle: Long)
    private data class CpuSample(val snapshot: ProcStatSnapshot, val timestampMs: Long)

    @Volatile private var cpuSample: CpuSample? = null
    @Volatile private var lastCpuPercent: Int = 0

    private fun readProcStatSnapshot(): ProcStatSnapshot? {
        // /proc/stat may not be readable on all vendor builds (sandboxed).
        return try {
            RandomAccessFile(File("/proc/stat"), "r").use { raf ->
                val line = raf.readLine() ?: return null
                if (!line.startsWith("cpu ")) return null
                val parts = line.split(Regex("\\s+")).drop(1).mapNotNull { it.toLongOrNull() }
                if (parts.size < 4) return null
                val total = parts.sum()
                val idle = parts.getOrElse(3) { 0L } + parts.getOrElse(4) { 0L }
                ProcStatSnapshot(total, idle)
            }
        } catch (_: Throwable) {
            null
        }
    }

    // --- Utils --------------------------------------------------------------

    private fun bytesToGb(bytes: Float): Float {
        val gb = bytes / BYTES_PER_GB
        // Round to one decimal — matches the "X.X GB" labels in the UI.
        return min(999f, (gb * 10f).toInt() / 10f)
    }

    companion object {
        private const val BYTES_PER_GB: Float = 1024f * 1024f * 1024f
    }
}
