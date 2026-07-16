package com.matox.nexcore.presentation.battery.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.matox.nexcore.ui.theme.MetricCyan
import com.matox.nexcore.ui.theme.NexCoreGreen

/**
 * "Battery Analytics" chart — the 24-hour level timeline.
 * Reuses [BatteryLineChart] for the canvas + glass card; just supplies
 * the title, palette, axis labels, and live-value string.
 */
@Composable
fun BatteryLevelChart(
    historyPercent: List<Int>,
    currentLevelPercent: Int,
    modifier: Modifier = Modifier,
) {
    val values = historyPercent.map { it.toFloat() }
    BatteryLineChart(
        title = "Battery Analytics",
        subtitle = "Last 24 hours · auto-updating",
        values = values,
        minValue = 0f,
        maxValue = 100f,
        primaryColor = NexCoreGreen,
        secondaryColor = MetricCyan,
        liveLabel = "${currentLevelPercent.coerceIn(0, 100)}% now",
        axisLabels = listOf("24h", "18h", "12h", "6h", "now"),
        modifier = modifier,
        ambientColor = NexCoreGreen,
    )
}
