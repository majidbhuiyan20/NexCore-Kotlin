package com.matox.nexcore.core.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * A single segment in a [DonutChart].
 *
 * [value] is relative (compared against the sum of all segments) — not
 * a percentage. Callers may pass raw GB counts, percentages, etc.
 */
data class DonutSegment(
    val value: Float,
    val color: Color,
) {
    init {
        require(value >= 0f) { "value must be >= 0, got $value" }
    }
}

/**
 * Generic multi-segment donut / progress ring chart.
 *
 * Each segment is drawn as a sweep arc starting from -90° (12 o'clock).
 * Adjacent segments are separated by a small [gapDegrees] so the ring
 * reads cleanly on a dark background.
 *
 * Anything composed in [content] is centered on top of the canvas.
 */
@Composable
fun DonutChart(
    segments: List<DonutSegment>,
    modifier: Modifier = Modifier,
    size: Dp = 140.dp,
    strokeWidth: Dp = 18.dp,
    trackColor: Color = Color(0xFF22304A),
    gapDegrees: Float = 4f,
    content: @Composable () -> Unit = {},
) {
    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val strokePx = strokeWidth.toPx()
            val inset = strokePx / 2f
            val arcSize = Size(this.size.width - strokePx, this.size.height - strokePx)
            val topLeft = Offset(inset, inset)
            val stroke = Stroke(width = strokePx)

            // Background track
            drawArc(
                color = trackColor,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = stroke,
            )

            val total = segments.sumOf { it.value.toDouble() }.toFloat()
            if (total <= 0f) return@Canvas

            var currentAngle = -90f
            segments.forEach { segment ->
                val proportion = segment.value / total
                val sweep = 360f * proportion
                // Effective sweep shrinks by gap on each end so segments don't visually merge
                val effectiveSweep = (sweep - gapDegrees).coerceAtLeast(0f)
                if (effectiveSweep > 0f) {
                    drawArc(
                        color = segment.color,
                        startAngle = currentAngle + gapDegrees / 2f,
                        sweepAngle = effectiveSweep,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = stroke,
                    )
                }
                currentAngle += sweep
            }
        }
        content()
    }
}