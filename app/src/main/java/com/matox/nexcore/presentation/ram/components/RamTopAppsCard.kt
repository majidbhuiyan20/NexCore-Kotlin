package com.matox.nexcore.presentation.ram.components

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Apps
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.matox.nexcore.domain.model.AppRamUsage
import com.matox.nexcore.ui.theme.CardStroke
import com.matox.nexcore.ui.theme.GlassHighlight
import com.matox.nexcore.ui.theme.MetricBlue
import com.matox.nexcore.ui.theme.MetricCyan
import com.matox.nexcore.ui.theme.MetricViolet
import com.matox.nexcore.ui.theme.NexCoreGreen
import com.matox.nexcore.ui.theme.Surface
import com.matox.nexcore.ui.theme.TextPrimary
import com.matox.nexcore.ui.theme.TextSecondary
import com.matox.nexcore.ui.theme.TrackGray

/**
 * Card listing the top RAM-consuming apps with their memory usage
 * displayed prominently.
 *
 * Each row visualises, in priority order:
 *  1. The app's name + a "system" pill if applicable, plus a small
 *     green "Live" dot when the app's process is currently visible
 *     to the OS (real PSS, not the on-disk proxy).
 *  2. A **big numeric value** (e.g. "412 MB" or "1.2 GB") — the
 *     app's PSS — styled in the accent color so the eye lands on
 *     it instantly.
 *  3. The same value expressed as a percentage of total device RAM
 *     so the user can compare rows.
 *  4. A horizontal bar showing relative usage vs. the top app.
 *  5. The package name in muted text below.
 *  6. A chevron arrow on the right — the entire row is clickable
 *     and opens a detailed bottom sheet via [onAppClick].
 *
 * The icon column uses the real PackageManager bitmap when present
 * (see [AppIconLoader]); falls back to a monogram tile otherwise.
 */
@Composable
fun RamTopAppsCard(
    apps: List<AppRamUsage>,
    totalRamMb: Long,
    appIcons: Map<String, Bitmap>,
    onAppClick: (AppRamUsage) -> Unit,
    modifier: Modifier = Modifier,
) {
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
        // Glass highlight strip on top.
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
            // Header row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(MetricViolet.copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Apps,
                        contentDescription = null,
                        tint = MetricViolet,
                        modifier = Modifier.size(20.dp),
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Top RAM Usage Apps",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                        ),
                        color = TextPrimary,
                    )
                    Text(
                        text = "${apps.size} apps · live · ranked by PSS",
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
                        text = "No foreground apps to report.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                    )
                }
                return
            }

            val maxPss = apps.maxOf { it.pssMb }.coerceAtLeast(1L)
            val safeTotal = totalRamMb.coerceAtLeast(1L)

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                apps.forEachIndexed { idx, app ->
                    AppRamRow(
                        app = app,
                        rank = idx + 1,
                        maxPssMb = maxPss,
                        totalRamMb = safeTotal,
                        icon = appIcons[app.packageName],
                        onClick = { onAppClick(app) },
                    )
                }
            }
        }
    }
}

@Composable
private fun AppRamRow(
    app: AppRamUsage,
    rank: Int,
    maxPssMb: Long,
    totalRamMb: Long,
    icon: Bitmap?,
    onClick: () -> Unit,
) {
    val weight = (app.pssMb.toFloat() / maxPssMb.toFloat()).coerceIn(0f, 1f)
    val percentOfTotal = ((app.pssMb.toFloat() / totalRamMb.toFloat()) * 100f)
        .coerceIn(0f, 100f)
    val monogram = app.displayName.firstOrNull()?.uppercase() ?: "?"

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
        // Icon column — real bitmap from PackageManager if available,
        // monogram tile otherwise.
        AppIconOrMonogram(
            monogram = monogram,
            icon = icon,
            isSystem = app.isSystem,
        )

        Spacer(modifier = Modifier.width(10.dp))

        // Middle column — name, package, bar.
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Rank chip
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
                if (app.isSystem) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(TrackGray.copy(alpha = 0.7f))
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                    ) {
                        Text(
                            text = "system",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary,
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = app.packageName,
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.height(8.dp))
            // Bigger, gradient bar.
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
                                colors = listOf(MetricCyan, MetricBlue, MetricViolet),
                            ),
                        ),
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            // Percentage-of-total label + Live indicator.
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = String.format("%.1f%% of total RAM", percentOfTotal),
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary,
                )
                if (app.hasRealPss) {
                    Spacer(modifier = Modifier.width(8.dp))
                    LiveDot()
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "live",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                        ),
                        color = NexCoreGreen,
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(10.dp))

        // Right column — the big numeric value + chevron.
        Column(
            horizontalAlignment = Alignment.End,
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(MetricBlue.copy(alpha = 0.12f))
                .padding(horizontal = 10.dp, vertical = 6.dp),
        ) {
            Text(
                text = formatMemory(app.pssMb),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                ),
                color = MetricBlue,
                maxLines = 1,
            )
            Text(
                text = "PSS",
                style = MaterialTheme.typography.labelSmall,
                color = MetricBlue.copy(alpha = 0.7f),
            )
        }

        Spacer(modifier = Modifier.width(4.dp))

        // Chevron — affordance for the click target.
        Icon(
            imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
            contentDescription = "Open ${app.displayName} details",
            tint = TextSecondary,
            modifier = Modifier.size(20.dp),
        )
    }
}

/**
 * Real PackageManager bitmap if available; otherwise a monogram tile
 * tinted by whether the app is system or user.
 */
@Composable
private fun AppIconOrMonogram(
    monogram: String,
    icon: Bitmap?,
    isSystem: Boolean,
) {
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
    val bg = if (isSystem) TrackGray else MetricBlue.copy(alpha = 0.20f)
    val fg = if (isSystem) TextSecondary else MetricBlue
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(bg),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = monogram,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
            ),
            color = fg,
        )
    }
}

/** Small green dot — the visual marker that a row's data is real-time. */
@Composable
private fun LiveDot() {
    Box(
        modifier = Modifier
            .size(6.dp)
            .clip(CircleShape)
            .background(NexCoreGreen),
    )
}

private fun formatMemory(mb: Long): String {
    val v = mb.toFloat()
    return when {
        v <= 0f -> "0 MB"
        v >= 1024f -> String.format("%.2f GB", v / 1024f)
        else -> "${mb.toInt()} MB"
    }
}

// Reserved for a future "open in app manager" action.
@Suppress("unused")
private val _keep: ImageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight