package com.matox.nexcore.presentation.cpu.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.matox.nexcore.presentation.battery.components.BatteryLineChart
import com.matox.nexcore.ui.theme.MetricOrange

/**
 * "CPU History" chart — 3-minute rolling line/area chart for the
 * device-wide CPU %. Reuses `BatteryLineChart`'s Canvas + glass card
 * but without a subtitle line duplicated in the title.
 */
@Composable
fun CpuHistoryChart(
    historyPercent: List<Int>,
    currentPercent: Int,
    modifier: Modifier = Modifier,
) {
    val values = historyPercent.map { it.toFloat() }
    BatteryLineChart(
        title = "CPU History",
        subtitle = "Last 3 minutes · auto-updating",
        values = values,
        minValue = 0f,
        maxValue = 100f,
        primaryColor = MetricOrange,
        secondaryColor = Color(0xFFFFB070),
        liveLabel = "${currentPercent.coerceIn(0, 100)}% now",
        axisLabels = listOf("3m", "2m", "90s", "30s", "now"),
        modifier = modifier,
        ambientColor = MetricOrange,
    )
}
