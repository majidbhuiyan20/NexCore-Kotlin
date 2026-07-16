package com.matox.nexcore.domain.model

/**
 * Snapshot of live device telemetry read from Android system APIs.
 *
 * All units are explicit so the presentation layer doesn't have to
 * re-format. Defaults to safe "no data" values so the UI never
 * crashes when a provider fails.
 */
data class DeviceMetrics(
    val ramUsedGb: Float = 0f,
    val ramTotalGb: Float = 0f,
    val ramPercent: Int = 0,
    val storageUsedGb: Float = 0f,
    val storageTotalGb: Float = 0f,
    val storagePercent: Int = 0,
    val batteryPercent: Int = 0,
    val batteryStatusLabel: String = "—",
    val cpuPercent: Int = 0,
    val temperatureC: Int = 0,
) {
    val ramFreeGb: Float get() = (ramTotalGb - ramUsedGb).coerceAtLeast(0f)
    val storageFreeGb: Float get() = (storageTotalGb - storageUsedGb).coerceAtLeast(0f)
}
