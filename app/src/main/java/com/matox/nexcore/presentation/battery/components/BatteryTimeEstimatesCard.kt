package com.matox.nexcore.presentation.battery.components

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
import androidx.compose.material.icons.outlined.Bedtime
import androidx.compose.material.icons.outlined.Movie
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material.icons.outlined.PhoneAndroid
import androidx.compose.material.icons.outlined.SportsEsports
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
import com.matox.nexcore.ui.theme.CardStroke
import com.matox.nexcore.ui.theme.GlassHighlight
import com.matox.nexcore.ui.theme.MetricBlue
import com.matox.nexcore.ui.theme.MetricCyan
import com.matox.nexcore.ui.theme.MetricOrange
import com.matox.nexcore.ui.theme.MetricViolet
import com.matox.nexcore.ui.theme.NexCoreGreen
import com.matox.nexcore.ui.theme.Surface
import com.matox.nexcore.ui.theme.TextPrimary
import com.matox.nexcore.ui.theme.TextSecondary

/**
 * "Estimated Battery Time" card — 5-tile grid (Screen / Video / Music /
 * Standby / Gaming) showing how many hours each typical activity will
 * drain the current charge.
 *
 * Estimates use a simple deterministic table of typical current draws
 * applied to the design capacity:
 *  - Screen On: 800 mA
 *  - Video: 600 mA
 *  - Music: 200 mA
 *  - Standby: 100 mA
 *  - Gaming: 1200 mA
 *
 * Tile renders "—" when capacity is missing or non-positive.
 */
@Composable
fun BatteryTimeEstimatesCard(
    capacityMah: Int,
    currentLevelPercent: Int,
    modifier: Modifier = Modifier,
) {
    // Scale estimates by current level so a 50% battery returns 50% of
    // the design-capacity-derived runtime — matches user intuition.
    val scale = (currentLevelPercent.coerceIn(0, 100) / 100f).coerceAtLeast(0.05f)

    val tiles = listOf(
        TimeTile("Screen On", Icons.Outlined.PhoneAndroid, NexCoreGreen, SCREEN_MA),
        TimeTile("Video Playback", Icons.Outlined.Movie, MetricViolet, VIDEO_MA),
        TimeTile("Music Playback", Icons.Outlined.MusicNote, MetricBlue, MUSIC_MA),
        TimeTile("Standby Time", Icons.Outlined.Bedtime, MetricCyan, STANDBY_MA),
        TimeTile("Gaming Time", Icons.Outlined.SportsEsports, MetricOrange, GAMING_MA),
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(22.dp),
                ambientColor = NexCoreGreen.copy(alpha = 0.15f),
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
                .height(50.dp)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(GlassHighlight, Color.Transparent),
                    ),
                ),
        )

        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Estimated Battery Time",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                ),
                color = TextPrimary,
            )
            Text(
                text = "Runtime by activity at current charge",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 2 rows × 3 columns — last cell is empty so we pad with a
            // placeholder Box to keep the grid alignment tidy.
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                tiles.take(3).forEach { tile ->
                    TimeTileView(
                        tile = tile,
                        capacityMah = capacityMah,
                        scale = scale,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                tiles.drop(3).take(2).forEach { tile ->
                    TimeTileView(
                        tile = tile,
                        capacityMah = capacityMah,
                        scale = scale,
                        modifier = Modifier.weight(1f),
                    )
                }
                // Spacer to keep the 2-row layout balanced.
                Box(modifier = Modifier.weight(1f))
            }
        }
    }
}

private data class TimeTile(
    val label: String,
    val icon: ImageVector,
    val accent: Color,
    val typicalDrawMa: Int,
)

@Composable
private fun TimeTileView(
    tile: TimeTile,
    capacityMah: Int,
    scale: Float,
    modifier: Modifier = Modifier,
) {
    val display = if (capacityMah <= 0 || tile.typicalDrawMa <= 0) {
        "—"
    } else {
        val minutes = (capacityMah * scale / tile.typicalDrawMa) * 60f
        formatMinutes(minutes.toInt())
    }

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(tile.accent.copy(alpha = 0.10f))
            .border(1.dp, tile.accent.copy(alpha = 0.28f), RoundedCornerShape(16.dp))
            .padding(horizontal = 12.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.Start,
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(tile.accent.copy(alpha = 0.20f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = tile.icon,
                contentDescription = null,
                tint = tile.accent,
                modifier = Modifier.size(16.dp),
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = tile.label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.SemiBold,
            ),
            color = tile.accent,
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = display,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
            ),
            color = TextPrimary,
        )
    }
}

private fun formatMinutes(totalMin: Int): String {
    if (totalMin <= 0) return "—"
    val h = totalMin / 60
    val m = totalMin % 60
    return when {
        h == 0 -> "${m}m"
        m == 0 -> "${h}h"
        else -> "${h}h ${m}m"
    }
}

private const val SCREEN_MA = 800
private const val VIDEO_MA = 600
private const val MUSIC_MA = 200
private const val STANDBY_MA = 100
private const val GAMING_MA = 1200
