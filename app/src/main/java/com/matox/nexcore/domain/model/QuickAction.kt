package com.matox.nexcore.domain.model

/**
 * One tile in the "Quick Actions" grid.
 *
 * [iconKey] is mapped to an actual [androidx.compose.ui.graphics.vector.ImageVector]
 * by `QuickActionIconMapper.kt` in the presentation layer.
 */
data class QuickAction(
    val id: String,
    val label: String,
    val iconKey: QuickActionIcon,
    val accent: MetricAccent,
)

enum class QuickActionIcon {
    STORAGE_ANALYZER,
    CLEANER,
    FILE_MANAGER,
    APP_MANAGER,
    BATTERY_MONITOR,
    NETWORK_MONITOR,
    WIFI_ANALYZER,
    SENSOR_MONITOR,
    PHONE_INFO,
    BACKUP_RESTORE,
}