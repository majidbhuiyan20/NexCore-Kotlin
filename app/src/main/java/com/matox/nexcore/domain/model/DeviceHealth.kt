package com.matox.nexcore.domain.model

/**
 * Aggregated device-health status. Used by the green banner under
 * the metric row.
 */
data class DeviceHealth(
    val title: String,
    val subtitle: String,
    val isHealthy: Boolean,
)