package com.matox.nexcore.presentation.battery.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.matox.nexcore.ui.theme.CardStroke
import com.matox.nexcore.ui.theme.GlassHighlight
import com.matox.nexcore.ui.theme.Surface
import com.matox.nexcore.ui.theme.TextPrimary
import com.matox.nexcore.ui.theme.TextSecondary

/**
 * Reusable line-chart card used by both the Battery Analytics and
 * Temperature History sections.
 *
 * Layout (identical for every use):
 *  - Header row: title + subtitle on the left, live value chip on the right.
 *  - 200 dp tall Canvas: grid + smooth cubic line + 3-stop gradient fill +
 *    per-sample dots + pulsing right-edge marker.
 *  - 5-tick x-axis along the bottom.
 *
 * Parameters:
 *  - [values]: sample values in chronological order. Decimated internally
 *    so the chart handles up to ~1500 samples smoothly.
 *  - [minValue] / [maxValue]: y-axis range. Clamped to keep the curve
 *    honest even when most samples are clustered at one end.
 *  - [primaryColor] / [secondaryColor]: gradient stops for the line + fill.
 *  - [liveLabel]: formatted live value rendered in the top-right chip.
 *  - [axisLabels]: 5 tick labels rendered along the bottom of the chart
 *    (left → right). E.g. `listOf("24h", "18h", "12h", "6h", "now")`.
 */
@Composable
fun BatteryLineChart(
    title: String,
    subtitle: String,
    values: List<Float>,
    minValue: Float,
    maxValue: Float,
    primaryColor: Color,
    secondaryColor: Color,
    liveLabel: String,
    axisLabels: List<String>,
    modifier: Modifier = Modifier,
    height: Dp = 200.dp,
    ambientColor: Color = primaryColor,
) {
    val latest = values.lastOrNull() ?: minValue
    val animatedLatest by animateFloatAsState(
        targetValue = latest,
        animationSpec = tween(durationMillis = 700, easing = LinearEasing),
        label = "line-chart-latest",
    )

    val pulse = rememberInfiniteTransition(label = "line-chart-pulse")
    val pulseScale by pulse.animateFloat(
        initialValue = 1f,
        targetValue = 2.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "line-chart-pulse-scale",
    )
    val pulseAlpha by pulse.animateFloat(
        initialValue = 0.55f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "line-chart-pulse-alpha",
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(22.dp),
                ambientColor = ambientColor.copy(alpha = 0.15f),
                spotColor = Color.Black.copy(alpha = 0.30f),
            )
            .clip(RoundedCornerShape(22.dp))
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Surface, Surface.copy(alpha = 0.92f)),
                ),
            )
            .border(1.dp, CardStroke, RoundedCornerShape(22.dp)),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(GlassHighlight, Color.Transparent),
                    ),
                ),
        )

        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                        ),
                        color = TextPrimary,
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(primaryColor),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = liveLabel,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                        ),
                        color = primaryColor,
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(height)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF0F1729))
                    .padding(horizontal = 12.dp, vertical = 14.dp),
            ) {
                if (values.isEmpty()) {
                    Text(
                        text = "Waiting for samples…",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        modifier = Modifier.align(Alignment.Center),
                    )
                } else {
                    Canvas(modifier = Modifier.fillMaxWidth().height(height - 28.dp)) {
                        val w = size.width
                        val h = size.height
                        val padX = 8f
                        val padY = 8f
                        val plotW = w - padX * 2
                        val plotH = h - padY * 2

                        // Background grid (5 horizontal lines).
                        val gridColor = Color(0xFF1F2A44)
                        for (i in 0..4) {
                            val y = padY + plotH * (i / 4f)
                            drawLine(
                                color = gridColor,
                                start = Offset(padX, y),
                                end = Offset(padX + plotW, y),
                                strokeWidth = 1f,
                            )
                        }

                        // Samples → (x, y)
                        val n = values.size
                        val stepX = if (n <= 1) 0f else plotW / (n - 1)
                        val range = (maxValue - minValue).coerceAtLeast(0.0001f)
                        val points = values.mapIndexed { idx, v ->
                            val clamped = v.coerceIn(minValue, maxValue)
                            val x = padX + stepX * idx
                            val y = padY + plotH * (1f - (clamped - minValue) / range)
                            Offset(x, y)
                        }

                        // Smooth cubic path.
                        val path = Path()
                        if (points.isNotEmpty()) {
                            path.moveTo(points.first().x, points.first().y)
                            for (i in 0 until points.lastIndex) {
                                val p0 = points[i]
                                val p1 = points[i + 1]
                                val midX = (p0.x + p1.x) / 2f
                                path.cubicTo(midX, p0.y, midX, p1.y, p1.x, p1.y)
                            }
                        }

                        // Area fill — 3-stop gradient.
                        val fillPath = Path().apply {
                            addPath(path)
                            if (points.isNotEmpty()) {
                                lineTo(points.last().x, padY + plotH)
                                lineTo(points.first().x, padY + plotH)
                                close()
                            }
                        }
                        drawPath(
                            path = fillPath,
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    primaryColor.copy(alpha = 0.55f),
                                    secondaryColor.copy(alpha = 0.30f),
                                    secondaryColor.copy(alpha = 0.05f),
                                ),
                                startY = padY,
                                endY = padY + plotH,
                            ),
                        )

                        // Soft under-stroke halo.
                        drawPath(
                            path = path,
                            color = primaryColor.copy(alpha = 0.30f),
                            style = Stroke(width = 9f),
                        )
                        // Crisp top line.
                        drawPath(
                            path = path,
                            brush = Brush.horizontalGradient(
                                colors = listOf(primaryColor, secondaryColor, primaryColor),
                            ),
                            style = Stroke(width = 3f),
                        )

                        // Per-sample dots (skip last).
                        for ((idx, p) in points.withIndex()) {
                            if (idx == points.lastIndex) continue
                            drawCircle(
                                color = primaryColor.copy(alpha = 0.7f),
                                radius = 2.2f,
                                center = p,
                            )
                        }

                        // Latest sample — pulse + halo + core.
                        val last = points.last()
                        drawLine(
                            color = primaryColor.copy(alpha = 0.45f),
                            start = Offset(last.x, padY),
                            end = Offset(last.x, padY + plotH),
                            strokeWidth = 1.5f,
                        )
                        drawCircle(
                            color = primaryColor.copy(alpha = pulseAlpha * 0.7f),
                            radius = 7f * pulseScale,
                            center = last,
                        )
                        drawCircle(
                            color = secondaryColor.copy(alpha = pulseAlpha * 0.4f),
                            radius = 10f * pulseScale,
                            center = last,
                        )
                        drawCircle(
                            color = primaryColor.copy(alpha = 0.30f),
                            radius = 11f,
                            center = last,
                        )
                        drawCircle(
                            color = Color.White,
                            radius = 5.5f,
                            center = last,
                        )
                        drawCircle(
                            color = primaryColor,
                            radius = 4f,
                            center = last,
                        )

                        // Suppress unused-variable warning on animatedLatest — it's
                        // used by the composable header chip above.
                        @Suppress("UNUSED_EXPRESSION") animatedLatest
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                axisLabels.forEachIndexed { idx, label ->
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = if (idx == axisLabels.lastIndex) FontWeight.Bold else FontWeight.Normal,
                        ),
                        color = if (idx == axisLabels.lastIndex) primaryColor else TextSecondary,
                    )
                }
            }
        }
    }
}
