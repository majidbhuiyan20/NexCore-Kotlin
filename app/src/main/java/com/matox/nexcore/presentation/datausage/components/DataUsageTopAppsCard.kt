package com.matox.nexcore.presentation.datausage.components

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.outlined.DataObject
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.matox.nexcore.domain.model.AppDataUsage
import com.matox.nexcore.ui.theme.CardStroke
import com.matox.nexcore.ui.theme.GlassHighlight
import com.matox.nexcore.ui.theme.MetricBlue
import com.matox.nexcore.ui.theme.MetricCyan
import com.matox.nexcore.ui.theme.MetricOrange
import com.matox.nexcore.ui.theme.Surface
import com.matox.nexcore.ui.theme.TextPrimary
import com.matox.nexcore.ui.theme.TextSecondary
import com.matox.nexcore.ui.theme.TrackGray

/**
 * "Top Data Consuming Apps" card — same shape as `BatteryTopAppsCard`
 * with palette recoloured to `MetricBlue → MetricCyan`.
 */
@Composable
fun DataUsageTopAppsCard(
    apps: List<AppDataUsage>,
    appIcons: Map<String, Bitmap>,
    onAppClick: (AppDataUsage) -> Unit,
    modifier: Modifier = Modifier,
) {
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
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(MetricBlue.copy(alpha = 0.20f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.DataObject,
                        contentDescription = null,
                        tint = MetricBlue,
                        modifier = Modifier.size(20.dp),
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Top Data Consuming Apps",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                        ),
                        color = TextPrimary,
                    )
                    Text(
                        text = "${apps.size} apps · ranked by total bytes",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (apps.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(72.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "No per-app data available.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                    )
                }
                return
            }

            val maxBytes = apps.maxOf { it.totalBytes }.coerceAtLeast(1L)

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                apps.forEachIndexed { idx, app ->
                    AppRow(
                        app = app,
                        rank = idx + 1,
                        maxBytes = maxBytes,
                        icon = appIcons[app.packageName],
                        onClick = { onAppClick(app) },
                    )
                }
            }
        }
    }
}

@Composable
private fun AppRow(
    app: AppDataUsage,
    rank: Int,
    maxBytes: Long,
    icon: Bitmap?,
    onClick: () -> Unit,
) {
    val weight = (app.totalBytes.toFloat() / maxBytes.toFloat()).coerceIn(0f, 1f)
    val monogram = app.displayName.firstOrNull()?.uppercase() ?: "?"
    val totalLabel = formatBytes(app.totalBytes)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Surface.copy(alpha = 0.5f))
            .border(1.dp, CardStroke, RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 10.dp),
        verticalAlignment = Alignment.Top,
    ) {
        AppIcon(monogram = monogram, icon = icon)

        Spacer(modifier = Modifier.width(10.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .clip(RoundedCornerShape(7.dp))
                        .background(MetricBlue.copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "$rank",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                        ),
                        color = MetricBlue,
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = app.displayName,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                    ),
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = app.packageName,
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(7.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(TrackGray),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(weight)
                        .height(7.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(MetricBlue.copy(alpha = 0.7f), MetricBlue, MetricCyan),
                            ),
                        ),
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row {
                Text(
                    text = "↓ ${formatBytes(app.mobileRxBytes + app.wifiRxBytes)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary,
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "↑ ${formatBytes(app.mobileTxBytes + app.wifiTxBytes)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary,
                )
            }
        }

        Spacer(modifier = Modifier.width(10.dp))

        Column(
            horizontalAlignment = Alignment.End,
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(MetricBlue.copy(alpha = 0.14f))
                .padding(horizontal = 10.dp, vertical = 6.dp),
        ) {
            Text(
                text = totalLabel,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                ),
                color = MetricBlue,
                maxLines = 1,
            )
            Text(
                text = "total",
                style = MaterialTheme.typography.labelSmall,
                color = MetricBlue.copy(alpha = 0.7f),
            )
        }
    }
}

@Composable
private fun AppIcon(monogram: String, icon: Bitmap?) {
    if (icon != null) {
        Image(
            bitmap = icon.asImageBitmap(),
            contentDescription = null,
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp)),
        )
        return
    }
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(MetricOrange.copy(alpha = 0.20f)),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = monogram,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
            ),
            color = MetricOrange,
        )
    }
}

private fun formatBytes(bytes: Long): String {
    val b = bytes.coerceAtLeast(0L)
    return when {
        b < 1024L -> "$b B"
        b < 1024L * 1024L -> "%.1f KB".format(b / 1024f)
        b < 1024L * 1024L * 1024L -> "%.1f MB".format(b / (1024f * 1024f))
        else -> "%.2f GB".format(b / (1024f * 1024f * 1024f))
    }
}
