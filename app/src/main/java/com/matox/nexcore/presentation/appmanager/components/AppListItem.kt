package com.matox.nexcore.presentation.appmanager.components

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
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.matox.nexcore.domain.model.AppIconRef
import com.matox.nexcore.domain.model.AppInfo
import com.matox.nexcore.ui.theme.CardStroke
import com.matox.nexcore.ui.theme.MetricBlue
import com.matox.nexcore.ui.theme.MetricOrange
import com.matox.nexcore.ui.theme.MetricPurple
import com.matox.nexcore.ui.theme.NexCoreGreen
import com.matox.nexcore.ui.theme.NexCoreGreenAccent
import com.matox.nexcore.ui.theme.Surface
import com.matox.nexcore.ui.theme.TextPrimary
import com.matox.nexcore.ui.theme.TextSecondary

/**
 * One row in the App Manager list.
 *
 * Layout (left → right):
 *   icon | name + verified badge + version·category + size·date | User/System pill + ⋮ + actions
 *
 * Actions:
 *   - User apps  → Open, Info, Uninstall (three outlined buttons)
 *   - System apps → Info, Disable (no Open)
 */
@Composable
fun AppListItem(
    app: AppInfo,
    modifier: Modifier = Modifier,
    onOpen: (AppInfo) -> Unit = {},
    onInfo: (AppInfo) -> Unit = {},
    onUninstall: (AppInfo) -> Unit = {},
    onDisable: (AppInfo) -> Unit = {},
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Surface)
            .border(1.dp, CardStroke, RoundedCornerShape(18.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // App icon
        AppIcon(iconRef = app.iconRef, packageName = app.packageName)

        Spacer(modifier = Modifier.width(12.dp))

        // Name + meta — flex
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = app.displayName,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = TextPrimary,
                    maxLines = 1,
                    modifier = Modifier.weight(1f, fill = false),
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Outlined.CheckCircle,
                    contentDescription = null,
                    tint = NexCoreGreen,
                    modifier = Modifier.size(14.dp),
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "${app.versionName}  •  ${app.categoryLabel}",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary,
                maxLines = 1,
            )
            Text(
                text = "${formatBytes(app.sizeBytes)}  •  ${formatDate(app.lastUpdatedEpochMs)}",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary,
                maxLines = 1,
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        // Right side: pill + overflow + actions
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        if (app.isSystem) MetricBlue.copy(alpha = 0.18f)
                        else NexCoreGreen.copy(alpha = 0.18f),
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp),
            ) {
                Text(
                    text = if (app.isSystem) "System" else "User",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = if (app.isSystem) MetricBlue else NexCoreGreenAccent,
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Overflow menu
            Box {
                var expanded by remember { mutableStateOf(false) }
                Icon(
                    imageVector = Icons.Outlined.MoreVert,
                    contentDescription = "More",
                    tint = TextSecondary,
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .clickable { expanded = true }
                        .padding(6.dp),
                )
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                ) {
                    if (!app.isSystem) {
                        DropdownMenuItem(
                            text = { Text("Open") },
                            leadingIcon = { Icon(Icons.AutoMirrored.Outlined.OpenInNew, contentDescription = null) },
                            onClick = { expanded = false; onOpen(app) },
                        )
                    }
                    DropdownMenuItem(
                        text = { Text("App info") },
                        leadingIcon = { Icon(Icons.Outlined.Info, contentDescription = null) },
                        onClick = { expanded = false; onInfo(app) },
                    )
                    if (!app.isSystem) {
                        DropdownMenuItem(
                            text = { Text("Uninstall") },
                            leadingIcon = { Icon(Icons.Outlined.Delete, contentDescription = null) },
                            onClick = { expanded = false; onUninstall(app) },
                        )
                    } else {
                        DropdownMenuItem(
                            text = { Text("Disable") },
                            leadingIcon = { Icon(Icons.Outlined.Block, contentDescription = null) },
                            onClick = { expanded = false; onDisable(app) },
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Inline action buttons (like the screenshot)
            if (!app.isSystem && app.hasLauncher) {
                ActionButton(
                    icon = Icons.AutoMirrored.Outlined.OpenInNew,
                    label = "Open",
                    tint = MetricPurple,
                    onClick = { onOpen(app) },
                )
                Spacer(modifier = Modifier.width(6.dp))
            }
            ActionButton(
                icon = Icons.Outlined.Info,
                label = "Info",
                tint = MetricPurple,
                onClick = { onInfo(app) },
            )
            Spacer(modifier = Modifier.width(6.dp))
            if (app.isSystem) {
                ActionButton(
                    icon = Icons.Outlined.Block,
                    label = "Disable",
                    tint = MetricOrange,
                    onClick = { onDisable(app) },
                )
            } else {
                ActionButton(
                    icon = Icons.Outlined.Delete,
                    label = "Uninstall",
                    tint = MetricOrange,
                    onClick = { onUninstall(app) },
                )
            }
        }
    }
}

@Composable
private fun AppIcon(iconRef: AppIconRef, packageName: String) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF18233C)),
        contentAlignment = Alignment.Center,
    ) {
        when (iconRef) {
            is AppIconRef.Loaded -> {
                Image(
                    bitmap = iconRef.bitmap.asImageBitmap(),
                    contentDescription = packageName,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp)),
                )
            }
            AppIconRef.Pending, AppIconRef.Failed -> {
                Icon(
                    imageVector = Icons.Outlined.MoreVert,
                    contentDescription = null,
                    tint = TextSecondary,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }
}

@Composable
private fun ActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    tint: Color,
    onClick: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 4.dp, vertical = 4.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = tint,
            modifier = Modifier.size(18.dp),
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.SemiBold),
            color = tint,
            maxLines = 1,
        )
    }
}

private fun formatBytes(bytes: Long): String {
    if (bytes <= 0) return "0 MB"
    val mb = bytes / (1024f * 1024f)
    return if (mb >= 1f) "${mb.toInt()} MB" else {
        val kb = bytes / 1024f
        "${kb.toInt()} KB"
    }
}

private fun formatDate(epochMs: Long): String {
    if (epochMs <= 0L) return "—"
    return try {
        val fmt = java.text.SimpleDateFormat("d MMM yyyy", java.util.Locale.getDefault())
        fmt.format(java.util.Date(epochMs))
    } catch (_: Throwable) {
        "—"
    }
}
