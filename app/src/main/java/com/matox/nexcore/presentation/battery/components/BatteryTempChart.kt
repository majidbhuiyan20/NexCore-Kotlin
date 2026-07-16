package com.matox.nexcore.presentation.battery.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.matox.nexcore.ui.theme.MetricOrange
import com.matox.nexcore.ui.theme.MetricRed
import kotlin.math.max
import kotlin.math.min

/**
 * "Temperature History" chart — 24-hour battery temperature timeline.
 * Reuses [BatteryLineChart]; y-axis clamps to 20°C..50°C so the curve
 * stays legible across realistic device temperatures.
 */
@Composable
fun BatteryTempChart(
    historyTempC: List<Float>,
    currentTempC: Float,
    modifier: Modifier = Modifier,
) {
    // Adaptive y-axis — never narrower than 20..50, but use observed
    // range if it's wider (catches thermal-runaway edge cases).
    val observedMin = historyTempC.minOrNull() ?: 20f
    val observedMax = historyTempC.maxOrNull() ?: 40f
    val yMin = min(20f, observedMin).coerceAtMost(observedMin - 2f)
    val yMax = max(50f, observedMax).coerceAtLeast(observedMax + 2f)

    BatteryLineChart(
        title = "Temperature History",
        subtitle = "Last 24 hours · thermal telemetry",
        values = historyTempC,
        minValue = yMin,
        maxValue = yMax,
        primaryColor = MetricOrange,
        secondaryColor = MetricRed,
        liveLabel = "%.1f°C now".format(currentTempC),
        axisLabels = listOf("24h", "18h", "12h", "6h", "now"),
        modifier = modifier,
        ambientColor = MetricOrange,
    )
}
