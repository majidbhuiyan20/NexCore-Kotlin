package com.matox.nexcore.presentation.storageanalyzer.components

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.matox.nexcore.core.ui.components.DonutChart
import com.matox.nexcore.core.ui.components.DonutSegment
import com.matox.nexcore.core.util.toColor
import com.matox.nexcore.domain.model.MetricAccent
import com.matox.nexcore.domain.model.StorageBreakdown
import com.matox.nexcore.ui.theme.CardStroke
import com.matox.nexcore.ui.theme.NexCoreGreen
import com.matox.nexcore.ui.theme.Surface
import com.matox.nexcore.ui.theme.TextPrimary
import com.matox.nexcore.ui.theme.TextSecondary

/**
 * Hero card on the Storage Analyzer screen: 7-segment donut chart
 * on the left + storage totals (Total / Used / Free) and a "Healthy"
 * badge on the right.
 *
 * The donut segments are sized by each category's share of total
 * storage so the chart and the tile grid stay in sync visually.
 */
@Composable
fun InternalStorageHero(
    breakdown: StorageBreakdown,
    modifier: Modifier = Modifier,
    donutSize: Dp = 168.dp,
) {
    val segments = breakdown.categories.map { cat ->
        DonutSegment(
            value = cat.usedGb.coerceAtLeast(0.01f),
            color = cat.accent.toColor(),
        )
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(Surface)
            .border(1.dp, CardStroke, RoundedCornerShape(22.dp))
            .padding(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Internal Storage",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = TextPrimary,
            )
            Spacer(modifier = Modifier.width(8.dp))
            // "Healthy" pill
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(NexCoreGreen.copy(alpha = 0.18f))
                    .padding(horizontal = 8.dp, vertical = 3.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(NexCoreGreen),
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Healthy",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = NexCoreGreen,
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Donut + center label
            Box(
                modifier = Modifier.size(donutSize),
                contentAlignment = Alignment.Center,
            ) {
                DonutChart(
                    segments = segments,
                    size = donutSize,
                    strokeWidth = 18.dp,
                    trackColor = Color(0xFF1F2A44),
                    gapDegrees = 2f,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${breakdown.usedPercent}%",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 28.sp,
                            ),
                            color = TextPrimary,
                        )
                        Text(
                            text = "Used",
                            style = MaterialTheme.typography.labelMedium,
                            color = TextSecondary,
                        )
                    }
                }
                // subtle outer green glow
                Canvas(modifier = Modifier.size(donutSize)) {
                    val ringRadius = this.size.minDimension / 2f
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                NexCoreGreen.copy(alpha = 0.18f),
                                Color.Transparent,
                            ),
                            center = Offset(this.size.width / 2f, this.size.height / 2f),
                            radius = ringRadius,
                        ),
                        radius = ringRadius * 1.05f,
                        center = Offset(this.size.width / 2f, this.size.height / 2f),
                    )
                }
            }

            Spacer(modifier = Modifier.width(18.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                StatRow(
                    label = "Total Storage",
                    value = formatGb(breakdown.internalTotalGb),
                    accent = MetricAccent.BLUE,
                    emphasis = false,
                )
                StatRow(
                    label = "Used",
                    value = formatGb(breakdown.internalUsedGb),
                    accent = MetricAccent.PURPLE,
                    emphasis = true,
                )
                StatRow(
                    label = "Free",
                    value = formatGb(breakdown.internalFreeGb),
                    accent = MetricAccent.GREEN,
                    emphasis = false,
                )
                Spacer(modifier = Modifier.height(2.dp))
                StorageUsageBar(progress = breakdown.usedPercent / 100f)
            }
        }
    }
}

@Composable
private fun StatRow(
    label: String,
    value: String,
    accent: MetricAccent,
    emphasis: Boolean,
) {
    val accentColor = accent.toColor()
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(accentColor),
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = if (emphasis) FontWeight.Bold else FontWeight.SemiBold,
                ),
                color = if (emphasis) TextPrimary else TextPrimary,
            )
        }
    }
}

@Composable
private fun StorageUsageBar(progress: Float) {
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(8.dp)
            .clip(RoundedCornerShape(4.dp)),
    ) {
        val barWidth = size.width
        // Background track
        drawRoundRect(
            color = Color(0xFF1F2A44),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f),
            size = Size(barWidth, size.height),
        )
        // Progress (green gradient)
        drawRoundRect(
            brush = Brush.horizontalGradient(
                colors = listOf(Color(0xFF34D399), NexCoreGreen, Color(0xFF15803D)),
            ),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f),
            size = Size(barWidth * progress.coerceIn(0f, 1f), size.height),
        )
    }
}

private fun formatGb(value: Float): String {
    val rounded = (value * 10f).toInt() / 10f
    return "${rounded} GB"
}