package com.matox.nexcore.presentation.ram.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.matox.nexcore.domain.model.RamSnapshot
import com.matox.nexcore.ui.theme.CardStroke
import com.matox.nexcore.ui.theme.MetricBlue
import com.matox.nexcore.ui.theme.MetricCyan
import com.matox.nexcore.ui.theme.MetricTeal
import com.matox.nexcore.ui.theme.Surface
import com.matox.nexcore.ui.theme.TextPrimary
import com.matox.nexcore.ui.theme.TextSecondary
import com.matox.nexcore.ui.theme.TrackGray

/**
 * Card showing the proportional split of RAM across four buckets:
 * Used · Buffers · Cached · Free.
 *
 * Uses a horizontal stacked bar at the top so the user can read the
 * proportions at a glance, followed by a labelled row per bucket.
 */
@Composable
fun RamBreakdownCard(
    snapshot: RamSnapshot,
    modifier: Modifier = Modifier,
) {
    val totalMb = (snapshot.totalGb * 1024f).toLong().coerceAtLeast(1L)
    val usedMb = (snapshot.usedGb * 1024f).toLong().coerceAtLeast(0L)
    val freeMb = (snapshot.availableGb * 1024f).toLong().coerceAtLeast(0L)
    // Subtract cached/buffers from "free" so they don't overlap —
    // buffers and cached count toward `used` in the proportional bar
    // since they're held by the kernel, not free for apps.
    val cached = snapshot.cachedMb.coerceAtLeast(0L)
    val buffers = snapshot.buffersMb.coerceAtLeast(0L)

    // The bar shows Used+Buffers+Cached vs Free. Within Used, we
    // split out Buffers and Cached as their own sub-segments.
    val usedPlusBuffersCached = usedMb
    val safeTotal = totalMb.coerceAtLeast(1L)

    val segments = listOf(
        Segment(label = "Used", mb = usedMb.coerceAtMost(safeTotal), color = MetricBlue),
        Segment(label = "Buffers", mb = buffers, color = MetricCyan),
        Segment(label = "Cached", mb = cached, color = MetricTeal),
        Segment(label = "Free", mb = freeMb.coerceAtLeast(0L), color = TrackGray),
    )
    val segmentTotal = segments.sumOf { it.mb }.coerceAtLeast(1L)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Surface)
            .border(1.dp, CardStroke, RoundedCornerShape(20.dp))
            .padding(16.dp),
    ) {
        Text(
            text = "Memory breakdown",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.SemiBold,
            ),
            color = TextPrimary,
        )
        Text(
            text = "What the kernel is holding right now",
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary,
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Stacked bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(14.dp)
                .clip(RoundedCornerShape(7.dp))
                .background(TrackGray),
        ) {
            segments.forEach { seg ->
                val weight = seg.mb.toFloat() / segmentTotal.toFloat()
                if (weight > 0f) {
                    Box(
                        modifier = Modifier
                            .weight(weight)
                            .fillMaxHeight()
                            .background(seg.color),
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Legend rows — colour swatch + label + value
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            LegendRow(MetricBlue, "Used", "${formatMb(usedMb)} · ${pctOf(usedMb, safeTotal)}%")
            LegendRow(MetricCyan, "Buffers", "${formatMb(buffers)} · ${pctOf(buffers, safeTotal)}%")
            LegendRow(MetricTeal, "Cached", "${formatMb(cached)} · ${pctOf(cached, safeTotal)}%")
            LegendRow(TrackGray, "Free", "${formatMb(freeMb)} · ${pctOf(freeMb, safeTotal)}%")
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Swap row — separate from the bar but informative.
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(TrackGray.copy(alpha = 0.4f))
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = "Swap",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
            )
            Text(
                text = "${formatMb(snapshot.swapTotalMb - snapshot.swapFreeMb)} / ${formatMb(snapshot.swapTotalMb)}",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                ),
                color = TextPrimary,
            )
        }

        // Avoid an "unused" lint warning on the helper — keep the
        // computed value visible in case future renderings need it.
        @Suppress("UNUSED_VARIABLE")
        val _u = usedPlusBuffersCached
    }
}

@Composable
private fun LegendRow(color: Color, label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(color),
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = TextPrimary,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.SemiBold,
            ),
            color = TextSecondary,
        )
    }
}

private data class Segment(val label: String, val mb: Long, val color: Color)

private fun formatMb(mb: Long): String {
    val v = mb.toFloat()
    return when {
        v >= 1024f -> String.format("%.1f GB", v / 1024f)
        v <= 0f -> "0 MB"
        else -> "${mb.toInt()} MB"
    }
}

private fun pctOf(part: Long, whole: Long): Int {
    if (whole <= 0L) return 0
    return ((part.toFloat() / whole.toFloat()) * 100f).toInt().coerceIn(0, 100)
}