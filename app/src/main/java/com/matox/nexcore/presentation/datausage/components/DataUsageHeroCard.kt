package com.matox.nexcore.presentation.datausage.components

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
import androidx.compose.material.icons.outlined.DataUsage
import androidx.compose.material.icons.outlined.SignalCellularAlt
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
import com.matox.nexcore.ui.theme.CardStroke
import com.matox.nexcore.ui.theme.GlassHighlight
import com.matox.nexcore.ui.theme.MetricBlue
import com.matox.nexcore.ui.theme.MetricCyan
import com.matox.nexcore.ui.theme.Surface
import com.matox.nexcore.ui.theme.TextPrimary
import com.matox.nexcore.ui.theme.TextSecondary

/**
 * Hero card for the Data Usage Monitor screen — total since-boot
 * consumption split across mobile + Wi-Fi.
 *
 *  ┌──────────────────────┬──────────────────────┐
 *  │ ▼ Mobile             │ ▼ Wi-Fi              │
 *  │ ↓ 24 MB  ↑ 8 MB      │ ↓ 312 MB  ↑ 5 MB     │
 *  └──────────────────────┴──────────────────────┘
 */
@Composable
fun DataUsageHeroCard(
    mobileRxBytes: Long,
    mobileTxBytes: Long,
    wifiRxBytes: Long,
    wifiTxBytes: Long,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(24.dp),
                ambientColor = MetricBlue.copy(alpha = 0.18f),
                spotColor = Color.Black.copy(alpha = 0.45f),
            )
            .clip(RoundedCornerShape(24.dp))
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Surface, Surface.copy(alpha = 0.92f)),
                ),
            )
            .border(1.dp, CardStroke, RoundedCornerShape(24.dp)),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(GlassHighlight, Color.Transparent),
                    ),
                ),
        )

        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MetricBlue.copy(alpha = 0.20f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.DataUsage,
                        contentDescription = null,
                        tint = MetricBlue,
                        modifier = Modifier.size(22.dp),
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Network since boot",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                        ),
                        color = TextPrimary,
                    )
                    Text(
                        text = "Real-time totals · mobile + Wi-Fi",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                NetworkCell(
                    icon = Icons.Outlined.SignalCellularAlt,
                    label = "Mobile",
                    rxBytes = mobileRxBytes,
                    txBytes = mobileTxBytes,
                    accent = MetricCyan,
                    modifier = Modifier.weight(1f),
                )
                NetworkCell(
                    icon = Icons.Outlined.Wifi,
                    label = "Wi-Fi",
                    rxBytes = wifiRxBytes,
                    txBytes = wifiTxBytes,
                    accent = MetricBlue,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun NetworkCell(
    icon: ImageVector,
    label: String,
    rxBytes: Long,
    txBytes: Long,
    accent: Color,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(accent.copy(alpha = 0.10f))
            .border(1.dp, accent.copy(alpha = 0.28f), RoundedCornerShape(18.dp))
            .padding(horizontal = 14.dp, vertical = 14.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(accent.copy(alpha = 0.20f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(18.dp),
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                ),
                color = accent,
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = "↓",
                style = MaterialTheme.typography.labelSmall,
                color = accent,
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = formatBytes(rxBytes),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                ),
                color = TextPrimary,
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = "↑",
                style = MaterialTheme.typography.labelSmall,
                color = accent.copy(alpha = 0.7f),
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = formatBytes(txBytes),
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                ),
                color = TextSecondary,
            )
        }
    }
}

/** Compact "X.X MB" / "X.X KB" formatter — 1 decimal place. */
private fun formatBytes(bytes: Long): String {
    val b = bytes.coerceAtLeast(0L)
    return when {
        b < 1024L -> "$b B"
        b < 1024L * 1024L -> "%.1f KB".format(b / 1024f)
        b < 1024L * 1024L * 1024L -> "%.1f MB".format(b / (1024f * 1024f))
        else -> "%.2f GB".format(b / (1024f * 1024f * 1024f))
    }
}
