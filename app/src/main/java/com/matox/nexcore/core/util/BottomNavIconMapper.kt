package com.matox.nexcore.core.util

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.MonitorHeart
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import com.matox.nexcore.domain.model.BottomNavIcon

fun BottomNavIcon.icon(): ImageVector = when (this) {
    BottomNavIcon.HOME -> Icons.Outlined.Home
    BottomNavIcon.FILES -> Icons.Outlined.Folder
    BottomNavIcon.APPS -> Icons.Outlined.MonitorHeart
    BottomNavIcon.SETTINGS -> Icons.Outlined.Settings
}