package com.matox.nexcore.domain.model

/**
 * Detailed battery info shown in the dedicated Battery card.
 */
data class BatteryDetails(
    val percent: Int,
    val isCharging: Boolean,
    val temperatureC: Int,
) {
    init {
        require(percent in 0..100) { "percent must be in 0..100, got $percent" }
    }
}