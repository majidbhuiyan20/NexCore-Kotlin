package com.matox.nexcore.core.util

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.Memory
import androidx.compose.material.icons.outlined.SdStorage
import androidx.compose.material.icons.outlined.Sensors
import androidx.compose.material.icons.outlined.Thermostat
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material.icons.outlined.DataUsage
import androidx.compose.material.icons.outlined.Wifi
import androidx.compose.ui.graphics.vector.ImageVector
import com.matox.nexcore.domain.model.MetricType

fun MetricType.icon(): ImageVector = when (this) {
    MetricType.RAM -> Icons.Outlined.Memory
    MetricType.STORAGE -> Icons.Outlined.SdStorage
    MetricType.BATTERY -> Icons.Outlined.Bolt
    MetricType.CPU -> Icons.Outlined.Speed
    MetricType.TEMPERATURE -> Icons.Outlined.Thermostat
    MetricType.DATA_USAGE -> Icons.Outlined.DataUsage
    MetricType.WIFI -> Icons.Outlined.Wifi
    MetricType.SENSORS -> Icons.Outlined.Sensors
}