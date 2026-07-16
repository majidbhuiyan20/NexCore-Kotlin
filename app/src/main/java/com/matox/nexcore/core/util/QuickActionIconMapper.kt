package com.matox.nexcore.core.util

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.Backup
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.CleaningServices
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.GraphicEq
import androidx.compose.material.icons.outlined.NetworkCheck
import androidx.compose.material.icons.outlined.PhoneAndroid
import androidx.compose.material.icons.outlined.PieChart
import androidx.compose.material.icons.outlined.Wifi
import androidx.compose.ui.graphics.vector.ImageVector
import com.matox.nexcore.domain.model.QuickActionIcon

fun QuickActionIcon.icon(): ImageVector = when (this) {
    QuickActionIcon.STORAGE_ANALYZER -> Icons.Outlined.PieChart
    QuickActionIcon.CLEANER -> Icons.Outlined.CleaningServices
    QuickActionIcon.FILE_MANAGER -> Icons.Outlined.Folder
    QuickActionIcon.APP_MANAGER -> Icons.Outlined.Apps
    QuickActionIcon.BATTERY_MONITOR -> Icons.Outlined.Bolt
    QuickActionIcon.NETWORK_MONITOR -> Icons.Outlined.NetworkCheck
    QuickActionIcon.WIFI_ANALYZER -> Icons.Outlined.Wifi
    QuickActionIcon.SENSOR_MONITOR -> Icons.Outlined.GraphicEq
    QuickActionIcon.PHONE_INFO -> Icons.Outlined.PhoneAndroid
    QuickActionIcon.BACKUP_RESTORE -> Icons.Outlined.Backup
}