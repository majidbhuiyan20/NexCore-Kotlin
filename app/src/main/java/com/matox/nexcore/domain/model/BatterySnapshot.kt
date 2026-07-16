package com.matox.nexcore.domain.model

/**
 * Snapshot of the device battery, polled every 3 s by [com.matox.nexcore.data.device.BatteryProvider].
 *
 * Contains everything the Battery Monitor screen needs in one struct so
 * the ViewModel can stay a thin pass-through:
 *  - [reading]: the live `BatteryManager` reading for this tick.
 *  - [historyPercent]: rolling buffer of last-percent samples (1 sample/min, 24 h coverage).
 *  - [historyTempC]: same shape, for temperature.
 *  - [charging]: derived charging-session metadata (start time, ETA, last full).
 *  - [health]: derived health metrics (cycles, wear, score, pattern).
 *  - [topApps]: estimated top-consuming apps for the list view.
 *  - [insights]: pre-synthesised AI-style recommendations.
 *  - [batteryCapacityMah]: design capacity in mAh (used for time estimates).
 */
data class BatterySnapshot(
    val reading: BatteryReading,
    val historyPercent: List<Int> = emptyList(),
    val historyTempC: List<Float> = emptyList(),
    val charging: ChargingInfo = ChargingInfo(),
    val health: BatteryHealth = BatteryHealth(),
    val topApps: List<BatteryAppUsage> = emptyList(),
    val insights: List<BatteryInsight> = emptyList(),
    val batteryCapacityMah: Int = 0,
)

/** Live `BatteryManager` reading. */
data class BatteryReading(
    val levelPercent: Int,
    val status: BatteryStatus,
    val plugType: PlugType,
    /** Heuristic — true when charging and |current| > 1500 mA. */
    val isFastCharging: Boolean = false,
    /** 0.1 °C resolution (BatteryManager.EXTRA_TEMPERATURE / 10). */
    val temperatureC: Float = 0f,
    val voltageMv: Int = 0,
    /** `BATTERY_PROPERTY_CURRENT_NOW` in milliamps (positive = charging in). */
    val currentNowMa: Int = 0,
    val technology: String = "—",
)

enum class BatteryStatus {
    CHARGING,
    DISCHARGING,
    FULL,
    NOT_CHARGING,
    UNKNOWN,
}

enum class PlugType {
    NONE,
    AC,
    USB,
    WIRELESS,
}

/** Charging session metadata, derived from state-diff over the polling loop. */
data class ChargingInfo(
    /** Wall-clock ms when the current charging session started, or null. */
    val chargingStartedMs: Long? = null,
    /** Wall-clock ms when the device was last observed at 100 %, or null. */
    val lastFullChargeMs: Long? = null,
    /** API 28+ estimate, minutes remaining; null on older devices or when not charging. */
    val estimatedTimeToFullMin: Int? = null,
    /** Whole minutes since the current charging session began. */
    val durationSoFarMin: Int = 0,
)

/** Derived battery health metrics — synthetic until we have real counters. */
data class BatteryHealth(
    val cyclesEstimate: Int = 0,
    val wearPercent: Int = 0,
    val chargingPattern: ChargingPattern = ChargingPattern.IRREGULAR,
    /** 0..100 — composite health score. */
    val healthScore: Int = 100,
    val healthLabel: String = "Good",
)

enum class ChargingPattern {
    OVERNIGHT,
    TOP_UP,
    FULL_CYCLE,
    IRREGULAR,
}

/** A single row in the "Top Battery Consuming Apps" list. */
data class BatteryAppUsage(
    val packageName: String,
    val displayName: String,
    /** 0..100 — share of recent drain. */
    val estimatedPct: Float,
    val estimatedMah: Float,
)

/** Pre-synthesised "Smart Recommendation" row. */
data class BatteryInsight(
    val accent: MetricAccent,
    val title: String,
    val subtitle: String,
    val iconKey: BatteryInsightIcon,
)

/** Icon key for [BatteryInsight] — mapped to a Material icon in the presentation layer. */
enum class BatteryInsightIcon {
    BRIGHTNESS,
    APPS,
    NIGHT,
    SAVER,
    COOL,
    FAST_CHARGE,
}
