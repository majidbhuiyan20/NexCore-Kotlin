package com.matox.nexcore.core.util

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.ui.graphics.vector.ImageVector
import com.matox.nexcore.domain.model.InfoCardData

fun InfoCardData.headerIcon(): ImageVector = when (id) {
    "info_apps" -> Icons.Outlined.Apps
    "info_data" -> Icons.Outlined.BarChart
    "info_notifications" -> Icons.Outlined.Notifications
    else -> Icons.Outlined.Apps
}