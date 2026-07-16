package com.matox.nexcore.domain.model

/**
 * Domain model for a system metric displayed on the dashboard
 * (RAM, Storage, Battery, CPU, Temperature, etc.).
 *
 * Pure data — no Compose/UI imports. Presentation layer maps
 * [accent] -> Color via a small theme extension.
 */
data class SystemMetric(
    val id: MetricType,
    val label: String,
    val valuePercent: Float,
    val primaryValue: String,
    val secondaryValue: String,
    val accent: MetricAccent,
) {
    init {
        require(valuePercent in 0f..100f) {
            "valuePercent must be between 0 and 100, got $valuePercent"
        }
    }
}

enum class MetricType {
    RAM,
    STORAGE,
    BATTERY,
    CPU,
    TEMPERATURE,
    DATA_USAGE,
    WIFI,
    SENSORS,
}

/**
 * Semantic color key, independent of Compose. The presentation layer
 * resolves these to actual [androidx.compose.ui.graphics.Color] values.
 */
enum class MetricAccent {
    BLUE,
    CYAN,
    PURPLE,
    VIOLET,
    GREEN,
    ORANGE,
    RED,
    PINK,
    TEAL,
}