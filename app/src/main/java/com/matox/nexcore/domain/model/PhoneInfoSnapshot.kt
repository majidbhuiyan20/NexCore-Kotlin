package com.matox.nexcore.domain.model

/**
 * Aggregated "About this device" payload used by the Phone Info screen.
 *
 * Each nested group corresponds to one section card on the screen and is
 * rendered as a single full-width card. Every field is a `String` because
 * the data is shown verbatim — no formatting, no units. Empty/blank fields
 * are displayed as "—" by the presentation layer.
 */
data class PhoneInfoSnapshot(
    val deviceName: String,
    val basic: BasicInfo,
    val hardware: HardwareInfo,
    val battery: BatteryInfo,
    val os: OsInfo,
    val network: NetworkInfo,
    val sensors: SensorsInfo,
    val uptimeSeconds: Long,
)

/**
 * Identifies the device + its place in the lineup.
 */
data class BasicInfo(
    val manufacturer: String,
    val brand: String,
    val model: String,
    val device: String,
    val product: String,
    val board: String,
    val hardware: String,
    val deviceCodeName: String,
    val fingerprint: String,
    val serial: String,
    val host: String,
    val user: String,
)

/**
 * SoC + display + storage capacity (no live values — these are the static
 * capabilities of the device).
 */
data class HardwareInfo(
    val cpuAbi: String,
    val cpuCores: Int,
    val supportedAbis: String,
    val processorName: String,
    val minSdk: Int,
    val targetSdk: Int,
    val sdkInt: Int,
    val javaVm: String,
    val screenResolution: String,
    val screenDensity: String,
    val refreshRateHz: String,
    val totalMemoryGb: String,
    val internalStorageGb: String,
)

/**
 * Battery health snapshot, derived from ACTION_BATTERY_CHANGED.
 */
data class BatteryInfo(
    val levelPercent: Int,
    val status: String,
    val plugged: String,
    val technology: String,
    val temperatureC: Int,
    val voltageMv: Int,
    val health: String,
)

/**
 * Operating system identity + security patch level.
 */
data class OsInfo(
    val androidVersion: String,
    val release: String,
    val securityPatch: String,
    val buildId: String,
    val buildDisplay: String,
    val buildDate: String,
    val kernelVersion: String,
    val javaVm: String,
    val bootloader: String,
)

/**
 * Connectivity — SIM, network type, MAC (best-effort, returns "—" when
 * restricted by Android privacy rules).
 */
data class NetworkInfo(
    val simOperator: String,
    val simState: String,
    val networkType: String,
    val dataState: String,
    val wifiMac: String,
    val ipAddress: String,
    val wifiSsid: String,
    val signalDbm: String,
)

/**
 * Sensor presence and (where applicable) version + vendor info.
 */
data class SensorsInfo(
    val accelerometer: String,
    val gyroscope: String,
    val magnetometer: String,
    val proximity: String,
    val lightSensor: String,
    val barometer: String,
    val stepCounter: String,
    val stepDetector: String,
    val gravity: String,
    val rotationVector: String,
    val totalSensorCount: Int,
)
