package com.matox.nexcore.presentation.datausage.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.outlined.ArrowDownward
import androidx.compose.material.icons.outlined.ArrowUpward
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
import com.matox.nexcore.ui.theme.MetricOrange
import com.matox.nexcore.ui.theme.MetricViolet
import com.matox.nexcore.ui.theme.Surface
import com.matox.nexcore.ui.theme.TextPrimary
import com.matox.nexcore.ui.theme.TextSecondary

/**
 * "Live Network Metrics" — 2×2 grid summarising Mobile total, Wi-Fi
 * total, Download total, Upload total. All "since boot".
 */
@Composable
fun DataUsageMetricsCard(
    mobileRxBytes: Long,
    mobileTxBytes: Long,
    wifiRxBytes: Long,
    wifiTxBytes: Long,
    modifier: Modifier = Modifier,
) {
    val totalMobile = mobileRxBytes + mobileTxBytes
    val totalWifi = wifiRxBytes + wifiTxBytes
    val totalRx = mobileRxBytes + wifiRxBytes
    val totalTx = mobileTxBytes + wifiTxBytes

    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(22.dp),
                ambientColor = MetricBlue.copy(alpha = 0.15f),
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
                text = "Live Network Metrics",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                ),
                color = TextPrimary,
            )
            Text(
                text = "Mobile · Wi-Fi · Download · Upload",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row {
                MetricCell(
                    label = "Total Mobile",
                    value = formatBytesMetric(totalMobile),
                    accent = MetricCyan,
                    icon = Icons.Outlined.SignalCellularAlt,
                    modifier = Modifier.weight(1f),
                )
                Spacer(modifier = Modifier.width(10.dp))
                MetricCell(
                    label = "Total Wi-Fi",
                    value = formatBytesMetric(totalWifi),
                    accent = MetricBlue,
                    icon = Icons.Outlined.Wifi,
                    modifier = Modifier.weight(1f),
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row {
                MetricCell(
                    label = "Downloaded",
                    value = formatBytesMetric(totalRx),
                    accent = MetricViolet,
                    icon = Icons.Outlined.ArrowDownward,
                    modifier = Modifier.weight(1f),
                )
                Spacer(modifier = Modifier.width(10.dp))
                MetricCell(
                    label = "Uploaded",
                    value = formatBytesMetric(totalTx),
                    accent = MetricOrange,
                    icon = Icons.Outlined.ArrowUpward,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun MetricCell(
    label: String,
    value: String,
    accent: Color,
    icon: ImageVector,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(accent.copy(alpha = 0.10f))
            .border(1.dp, accent.copy(alpha = 0.28f), RoundedCornerShape(16.dp))
            .padding(horizontal = 12.dp, vertical = 12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(RoundedCornerShape(9.dp))
                    .background(accent.copy(alpha = 0.20f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(16.dp),
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                ),
                color = accent,
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
            ),
            color = TextPrimary,
            maxLines = 1,
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = "since boot",
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary,
        )
    }
}

/** "X.X MB" / "X.X GB" formatter — adaptive threshold. */
private fun formatBytesMetric(bytes: Long): String {
    val b = bytes.coerceAtLeast(0L)
    return when {
        b < 1024L -> "$b B"
        b < 1024L * 1024L -> "%.1f KB".format(b / 1024f)
        b < 1024L * 1024L * 1024L -> "%.1f MB".format(b / (1024f * 1024f))
        else -> "%.2f GB".format(b / (1024f * 1024f * 1024f))
    }
}
