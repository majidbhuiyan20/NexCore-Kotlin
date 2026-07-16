package com.matox.nexcore.presentation.ram.components

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.provider.Settings
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
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.Cached
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Memory
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.outlined.SdStorage
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.matox.nexcore.domain.model.AppRamUsage
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
 * Bottom sheet showing detailed memory information for a single app.
 *
 * Triggered by tapping a row in `RamTopAppsCard`. The sheet content:
 *  - Drag handle.
 *  - Large app icon (64 dp) + display name + package name + "system"
 *    or "user" pill.
 *  - Big PSS value with subtitle "Proportional Set Size".
 *  - Detail rows: PSS, private dirty, share of total RAM, share of
 *    top app, is system.
 *  - Two action buttons:
 *      - **Open app info** — fires
 *        `Settings.ACTION_APPLICATION_DETAILS_SETTINGS` for the
 *        package.
 *      - **Force stop** — stub that triggers the snackbar via the
 *        supplied [onForceStop] callback.
 *
 * The sheet dismisses on scrim tap, drag, or back press.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppMemoryDetailSheet(
    app: AppRamUsage,
    icon: Bitmap?,
    totalRamMb: Long,
    topAppPssMb: Long,
    onDismiss: () -> Unit,
    onForceStop: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Surface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
        ) {
            // ---- App header ------------------------------------------------
            Row(verticalAlignment = Alignment.CenterVertically) {
                AppIconLarge(monogram = app.displayName.firstOrNull()?.uppercase() ?: "?")
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = app.displayName,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                        ),
                        color = TextPrimary,
                    )
                    Text(
                        text = app.packageName,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (app.isSystem) MetricOrange.copy(alpha = 0.18f)
                                    else NexCoreGreen.copy(alpha = 0.18f),
                                )
                                .padding(horizontal = 8.dp, vertical = 3.dp),
                        ) {
                            Text(
                                text = if (app.isSystem) "system" else "user",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.SemiBold,
                                ),
                                color = if (app.isSystem) MetricOrange else NexCoreGreen,
                            )
                        }
                        if (app.hasRealPss) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(NexCoreGreen.copy(alpha = 0.18f))
                                    .padding(horizontal = 8.dp, vertical = 3.dp),
                            ) {
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
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // ---- Hero PSS number -------------------------------------------
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                MetricViolet.copy(alpha = 0.18f),
                                MetricBlue.copy(alpha = 0.10f),
                            ),
                        ),
                    )
                    .border(1.dp, MetricViolet.copy(alpha = 0.35f), RoundedCornerShape(18.dp))
                    .padding(16.dp),
            ) {
                Column {
                    Text(
                        text = "Proportional Set Size",
                        style = MaterialTheme.typography.labelMedium,
                        color = MetricViolet,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = formatMemory(app.pssMb),
                        style = MaterialTheme.typography.displaySmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 40.sp,
                        ),
                        color = TextPrimary,
                    )
                    val pctOfTotal = if (totalRamMb > 0L) {
                        ((app.pssMb.toFloat() / totalRamMb.toFloat()) * 100f).toInt().coerceIn(0, 100)
                    } else 0
                    val pctOfTop = if (topAppPssMb > 0L) {
                        ((app.pssMb.toFloat() / topAppPssMb.toFloat()) * 100f).toInt().coerceIn(0, 100)
                    } else 0
                    Text(
                        text = "$pctOfTotal% of total RAM · $pctOfTop% of top app",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // ---- Detail rows -----------------------------------------------
            DetailRow(
                icon = Icons.Outlined.Memory,
                label = "PSS",
                value = formatMemory(app.pssMb),
                accent = MetricBlue,
            )
            DetailRow(
                icon = Icons.Outlined.Cached,
                label = "Private dirty",
                value = if (app.privateDirtyMb > 0L) formatMemory(app.privateDirtyMb) else "—",
                accent = MetricCyan,
            )
            DetailRow(
                icon = Icons.Outlined.SdStorage,
                label = "Total RAM",
                value = formatMemory(totalRamMb),
                accent = MetricOrange,
            )
            DetailRow(
                icon = Icons.Outlined.Info,
                label = "Source",
                value = if (app.hasRealPss) "Live PSS readout" else "On-disk footprint estimate",
                accent = MetricViolet,
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ---- Action buttons --------------------------------------------
            ActionButton(
                label = "Open app info",
                subtitle = "System settings for ${app.packageName}",
                icon = Icons.AutoMirrored.Outlined.OpenInNew,
                accent = MetricBlue,
                onClick = {
                    runCatching {
                        val intent = Intent(
                            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                            Uri.parse("package:${app.packageName}"),
                        ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(intent)
                    }
                },
            )

            Spacer(modifier = Modifier.height(10.dp))

            ActionButton(
                label = "Force stop",
                subtitle = "Stub — Google discourages this on Android 8+",
                icon = Icons.Outlined.Bolt,
                accent = MetricOrange,
                onClick = onForceStop,
            )

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun AppIconLarge(monogram: String) {
    Box(
        modifier = Modifier
            .size(64.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MetricViolet.copy(alpha = 0.20f)),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = monogram,
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
            ),
            color = MetricViolet,
        )
    }
}

@Composable
private fun DetailRow(
    icon: ImageVector,
    label: String,
    value: String,
    accent: Color,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(accent.copy(alpha = 0.18f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accent,
                modifier = Modifier.size(18.dp),
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
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
            color = accent,
        )
    }
}

@Composable
private fun ActionButton(
    label: String,
    subtitle: String,
    icon: ImageVector,
    accent: Color,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(accent.copy(alpha = 0.15f))
            .border(1.dp, accent.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(accent.copy(alpha = 0.25f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accent,
                modifier = Modifier.size(18.dp),
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.SemiBold,
                ),
                color = TextPrimary,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary,
            )
        }
    }
}

private fun formatMemory(mb: Long): String {
    val v = mb.toFloat()
    return when {
        v <= 0f -> "0 MB"
        v >= 1024f -> String.format("%.2f GB", v / 1024f)
        else -> "${mb.toInt()} MB"
    }
}

// Keep AutoAwesome in scope for the upcoming "AI summary" header.
@Suppress("unused")
private val _keep: ImageVector = Icons.Outlined.AutoAwesome