package com.matox.nexcore.presentation.ram.components

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PieChart
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.matox.nexcore.core.ui.components.DonutChart
import com.matox.nexcore.core.ui.components.DonutSegment
import com.matox.nexcore.core.ui.components.IconChip
import com.matox.nexcore.domain.model.RamSnapshot
import com.matox.nexcore.ui.theme.CardStroke
import com.matox.nexcore.ui.theme.GlassHighlight
import com.matox.nexcore.ui.theme.MetricBlue
import com.matox.nexcore.ui.theme.MetricCyan
import com.matox.nexcore.ui.theme.MetricOrange
import com.matox.nexcore.ui.theme.MetricViolet
import com.matox.nexcore.ui.theme.Surface
import com.matox.nexcore.ui.theme.TextPrimary
import com.matox.nexcore.ui.theme.TextSecondary
import com.matox.nexcore.ui.theme.TrackGray

/**
 * "Memory Distribution" card — a donut chart showing the four-way
 * split between Apps / Cached / System (reserved) / Free.
 *
 * Reuses the project's generic [DonutChart] for the ring. Center
 * slot shows the headline "X.X GB / Y.Y GB used" so the user sees
 * the headline number in the donut's hole, mirroring how Samsung /
 * Pixel memory widgets present this data.
 *
 * The four legend chips below the donut show the same colour as
 * the matching segment + the segment's percentage of total RAM.
 */
@Composable
fun RamDistributionCard(
    snapshot: RamSnapshot,
    modifier: Modifier = Modifier,
) {
    val totalMb = (snapshot.totalGb * 1024f).toLong().coerceAtLeast(1L)
    val appsMb = snapshot.topApps.sumOf { it.pssMb }.coerceAtLeast(0L)
    val cachedMb = snapshot.cachedMb.coerceAtLeast(0L)
    val buffersMb = snapshot.buffersMb.coerceAtLeast(0L)
    val swapUsedMb = (snapshot.swapTotalMb - snapshot.swapFreeMb).coerceAtLeast(0L)
    val reservedMb = buffersMb + swapUsedMb
    val usedMb = (snapshot.usedGb * 1024f).toLong().coerceAtLeast(0L)
    val known = appsMb + cachedMb + reservedMb
    val freeMb = (totalMb - usedMb).coerceAtLeast(0L).let { free ->
        // Ensure the donut sums to `totalMb`. If the kernel-reported
        // "free" doesn't quite line up with the residual after the
        // three explicit buckets, fall back to the actual free figure.
        if (free + known >= totalMb) free else (totalMb - known).coerceAtLeast(0L)
    }

    val segments = listOf(
        DonutSegment(value = appsMb.toFloat(), color = MetricBlue),
        DonutSegment(value = cachedMb.toFloat(), color = MetricViolet),
        DonutSegment(value = reservedMb.toFloat(), color = MetricOrange),
        DonutSegment(value = freeMb.toFloat(), color = MetricCyan),
    )
    val sumMb = (appsMb + cachedMb + reservedMb + freeMb).coerceAtLeast(1L)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(22.dp),
                ambientColor = MetricViolet.copy(alpha = 0.15f),
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
        // Glass highlight.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(GlassHighlight, Color.Transparent),
                    ),
                ),
        )

        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconChip(
                    icon = Icons.Outlined.PieChart,
                    accent = MetricViolet,
                    size = 40.dp,
                    iconSize = 22.dp,
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Memory Distribution",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                        ),
                        color = TextPrimary,
                    )
                    Text(
                        text = "Apps · Cached · System · Free",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Donut + center label
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                DonutChart(
                    segments = segments,
                    size = 180.dp,
                    strokeWidth = 22.dp,
                    trackColor = TrackGray,
                    gapDegrees = 4f,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${snapshot.percent}%",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                            ),
                            color = TextPrimary,
                        )
                        Text(
                            text = "of ${formatGb(snapshot.totalGb)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Legend chips — 2x2 grid
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    LegendChip(
                        label = "Apps",
                        valueMb = appsMb,
                        totalMb = sumMb,
                        color = MetricBlue,
                        modifier = Modifier.weight(1f),
                    )
                    LegendChip(
                        label = "Cached",
                        valueMb = cachedMb,
                        totalMb = sumMb,
                        color = MetricViolet,
                        modifier = Modifier.weight(1f),
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    LegendChip(
                        label = "Reserved",
                        valueMb = reservedMb,
                        totalMb = sumMb,
                        color = MetricOrange,
                        modifier = Modifier.weight(1f),
                    )
                    LegendChip(
                        label = "Free",
                        valueMb = freeMb,
                        totalMb = sumMb,
                        color = MetricCyan,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
private fun LegendChip(
    label: String,
    valueMb: Long,
    totalMb: Long,
    color: Color,
    modifier: Modifier = Modifier,
) {
    val pct = ((valueMb.toFloat() / totalMb.toFloat()) * 100f).toInt().coerceIn(0, 100)
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(color.copy(alpha = 0.12f))
            .border(1.dp, color.copy(alpha = 0.30f), RoundedCornerShape(12.dp))
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color),
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.SemiBold,
                ),
                color = color,
            )
            Text(
                text = "$pct%",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary,
            )
        }
        Text(
            text = formatMb(valueMb),
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.SemiBold,
            ),
            color = TextPrimary,
        )
    }
}

private fun formatMb(mb: Long): String {
    val v = mb.toFloat()
    return when {
        v >= 1024f -> String.format("%.1f GB", v / 1024f)
        v <= 0f -> "0 MB"
        else -> "${mb.toInt()} MB"
    }
}

private fun formatGb(v: Float): String {
    if (v <= 0f) return "0.0 GB"
    val rounded = (v * 10f).toInt() / 10f
    return "$rounded GB"
}