package com.matox.nexcore.presentation.wifi.components

import android.graphics.Bitmap
import androidx.compose.foundation.Image
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.matox.nexcore.domain.model.AppTrafficRow
import com.matox.nexcore.ui.theme.CardStroke
import com.matox.nexcore.ui.theme.GlassHighlight
import com.matox.nexcore.ui.theme.MetricBlue
import com.matox.nexcore.ui.theme.MetricCyan
import com.matox.nexcore.ui.theme.MetricViolet
import com.matox.nexcore.ui.theme.Surface
import com.matox.nexcore.ui.theme.TextPrimary
import com.matox.nexcore.ui.theme.TextSecondary
import com.matox.nexcore.ui.theme.TrackGray

/**
 * "App Traffic over WiFi" card — list of the top ~5 packages
 * currently moving bytes over the WiFi link.
 *
 * Each row shows the app icon (or monogram fallback), display name,
 * rx / tx byte totals, and a tiny dual bar (rx above tx) where the
 * larger total saturates the bar.
 *
 * Mirrors `BatteryTopAppsCard` so the WiFi page reads as a familiar
 * pattern.
 */
@Composable
fun WifiAppTrafficCard(
    apps: List<AppTrafficRow>,
    appIcons: Map<String, Bitmap>,
    modifier: Modifier = Modifier,
) {
    val maxTotal = apps.maxOfOrNull { it.rxBytes + it.txBytes }?.coerceAtLeast(1L) ?: 1L

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
                text = "App Traffic over WiFi",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                ),
                color = TextPrimary,
            )
            Text(
                text = "Top packages by recent bytes moved",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (apps.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "No app traffic recorded yet",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                    )
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    apps.forEach { app ->
                        AppTrafficRow(
                            app = app,
                            icon = appIcons[app.packageName],
                            maxTotalBytes = maxTotal,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AppTrafficRow(
    app: AppTrafficRow,
    icon: Bitmap?,
    maxTotalBytes: Long,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = 0.04f))
            .border(1.dp, CardStroke, RoundedCornerShape(16.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Icon — or monogram fallback.
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(MetricViolet.copy(alpha = 0.20f)),
            contentAlignment = Alignment.Center,
        ) {
            if (icon != null) {
                Image(
                    bitmap = icon.asImageBitmap(),
                    contentDescription = app.displayName,
                    modifier = Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(6.dp)),
                )
            } else {
                Text(
                    text = monogram(app.displayName),
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                    ),
                    color = MetricViolet,
                )
            }
        }

        Spacer(modifier = Modifier.width(10.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = app.displayName,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                    ),
                    color = TextPrimary,
                )
                if (app.packageName.startsWith("com.android") || app.packageName.startsWith("android") || app.packageName.contains("google.android.gms")) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(MetricBlue.copy(alpha = 0.20f))
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                    ) {
                        Text(
                            text = "system",
                            style = MaterialTheme.typography.labelSmall,
                            color = MetricBlue,
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            // Dual rx / tx bars.
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                TinyBar(
                    bytes = app.rxBytes,
                    max = maxTotalBytes,
                    accent = MetricBlue,
                    label = "↓ ${formatBytes(app.rxBytes)}",
                )
                TinyBar(
                    bytes = app.txBytes,
                    max = maxTotalBytes,
                    accent = MetricCyan,
                    label = "↑ ${formatBytes(app.txBytes)}",
                )
            }
        }
    }
}

@Composable
private fun TinyBar(
    bytes: Long,
    max: Long,
    accent: Color,
    label: String,
) {
    val pct = (bytes.toFloat() / max.toFloat()).coerceIn(0f, 1f)
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .weight(1f)
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(TrackGray),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(pct)
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(accent.copy(alpha = 0.6f), accent),
                        ),
                    ),
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary,
        )
    }
}

/** First 1–2 letters of the display name — used when the icon
 *  bitmap hasn't been resolved yet. */
private fun monogram(displayName: String): String {
    val cleaned = displayName.trim().ifBlank { "?" }
    return cleaned.take(2).uppercase()
}

/** Convert raw bytes into the largest unit that still yields a
 *  >= 1 value. Simple KiB/MiB/GiB stepping. */
private fun formatBytes(bytes: Long): String {
    if (bytes <= 0L) return "0 B"
    val units = arrayOf("B", "KB", "MB", "GB", "TB")
    var value = bytes.toDouble()
    var unit = 0
    while (value >= 1024.0 && unit < units.lastIndex) {
        value /= 1024.0
        unit++
    }
    return if (unit == 0) {
        "${bytes} ${units[unit]}"
    } else {
        "%.1f %s".format(value, units[unit])
    }
}