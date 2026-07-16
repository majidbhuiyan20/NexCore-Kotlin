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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Cached
import androidx.compose.material.icons.outlined.Memory
import androidx.compose.material.icons.outlined.SdStorage
import androidx.compose.material.icons.outlined.Wifi
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
 * 2×2 grid of premium cards breaking down the four RAM buckets:
 *  - **Used** (blue, Memory icon)
 *  - **Free** (cyan, Memory icon, inverted)
 *  - **Cached** (violet, Cached icon)
 *  - **Reserved / System** (orange, SdStorage icon — buffers + swap
 *    used, which is held by the kernel)
 *
 * Each card: glass background, soft shadow, accent icon chip, big
 * value (24 sp bold), percentage of total RAM, mini progress bar.
 *
 * Outer section uses a glassy surface so the 2×2 grid sits on a
 * single coherent card surface.
 */
@Composable
fun RamBreakdownCard(
    snapshot: RamSnapshot,
    modifier: Modifier = Modifier,
) {
    val totalMb = (snapshot.totalGb * 1024f).toLong().coerceAtLeast(1L)
    val usedMb = (snapshot.usedGb * 1024f).toLong().coerceAtLeast(0L)
    val freeMb = (snapshot.availableGb * 1024f).toLong().coerceAtLeast(0L)
    val cachedMb = snapshot.cachedMb.coerceAtLeast(0L)
    val buffersMb = snapshot.buffersMb.coerceAtLeast(0L)
    val swapUsedMb = (snapshot.swapTotalMb - snapshot.swapFreeMb).coerceAtLeast(0L)
    val reservedMb = buffersMb + swapUsedMb

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
        // Top glass highlight.
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
            // Section header.
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconChip(
                    icon = Icons.Outlined.SdStorage,
                    accent = MetricViolet,
                    size = 40.dp,
                    iconSize = 22.dp,
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Memory Breakdown",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                        ),
                        color = TextPrimary,
                    )
                    Text(
                        text = "Where the kernel is holding your RAM",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 2x2 grid — each row holds two cards.
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                BreakdownCell(
                    label = "Used",
                    valueMb = usedMb,
                    totalMb = totalMb,
                    accent = MetricBlue,
                    icon = Icons.Outlined.Memory,
                    modifier = Modifier.weight(1f),
                )
                BreakdownCell(
                    label = "Free",
                    valueMb = freeMb,
                    totalMb = totalMb,
                    accent = MetricCyan,
                    icon = Icons.Outlined.Memory,
                    modifier = Modifier.weight(1f),
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                BreakdownCell(
                    label = "Cached",
                    valueMb = cachedMb,
                    totalMb = totalMb,
                    accent = MetricViolet,
                    icon = Icons.Outlined.Cached,
                    modifier = Modifier.weight(1f),
                )
                BreakdownCell(
                    label = "Reserved",
                    valueMb = reservedMb,
                    totalMb = totalMb,
                    accent = MetricOrange,
                    icon = Icons.Outlined.Wifi,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun BreakdownCell(
    label: String,
    valueMb: Long,
    totalMb: Long,
    accent: Color,
    icon: ImageVector,
    modifier: Modifier = Modifier,
) {
    val pct = ((valueMb.toFloat() / totalMb.toFloat()) * 100f).toInt().coerceIn(0, 100)
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(accent.copy(alpha = 0.10f))
            .border(1.dp, accent.copy(alpha = 0.30f), RoundedCornerShape(18.dp))
            .padding(horizontal = 12.dp, vertical = 12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconChip(
                icon = icon,
                accent = accent,
                size = 32.dp,
                iconSize = 16.dp,
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.SemiBold,
                ),
                color = TextPrimary,
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = formatMb(valueMb),
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
            ),
            color = TextPrimary,
        )
        Text(
            text = "$pct% of total",
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(TrackGray),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(pct / 100f)
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(accent.copy(alpha = 0.55f), accent),
                        ),
                    ),
            )
        }
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