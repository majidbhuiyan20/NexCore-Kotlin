package com.matox.nexcore.presentation.storageanalyzer.components

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
import androidx.compose.material.icons.outlined.Android
import androidx.compose.material.icons.outlined.AudioFile
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.MovieCreation
import androidx.compose.material.icons.outlined.PictureAsPdf
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.matox.nexcore.core.ui.components.IconChip
import com.matox.nexcore.core.util.toColor
import com.matox.nexcore.domain.model.LargeFileEntry
import com.matox.nexcore.ui.theme.CardStroke
import com.matox.nexcore.ui.theme.Surface
import com.matox.nexcore.ui.theme.TextPrimary
import com.matox.nexcore.ui.theme.TextSecondary

/**
 * Card listing the top large files found on internal storage. Each
 * row has a gradient icon chip + file name + right-aligned size.
 */
@Composable
fun TopLargeFilesCard(
    files: List<LargeFileEntry>,
    modifier: Modifier = Modifier,
    onFileClick: (LargeFileEntry) -> Unit = {},
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(Surface)
            .border(1.dp, CardStroke, RoundedCornerShape(22.dp))
            .padding(16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column {
                Text(
                    text = "Top Large Files",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = TextPrimary,
                )
                Text(
                    text = "${files.size} files taking the most space",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary,
                )
            }
            Text(
                text = "See all",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                color = com.matox.nexcore.ui.theme.NexCoreGreen,
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            files.forEach { file ->
                LargeFileRow(
                    file = file,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    onClick = { onFileClick(file) },
                )
            }
        }
    }
}

@Composable
private fun LargeFileRow(
    file: LargeFileEntry,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val accent = file.accent.toColor()
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF18233C))
            .border(1.dp, CardStroke, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconChip(
            icon = iconFor(file),
            accent = accent,
            size = 36.dp,
            iconSize = 18.dp,
            backgroundAlpha = 0.22f,
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = file.name,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = TextPrimary,
                maxLines = 1,
            )
            Text(
                text = fileSubtitle(file),
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary,
                maxLines = 1,
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = formatSize(file.sizeGb),
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            color = TextPrimary,
            maxLines = 1,
        )
    }
}

private fun iconFor(file: LargeFileEntry) = when {
    file.isApk -> Icons.Outlined.Android
    file.name.endsWith(".mp4", true) || file.name.endsWith(".mkv", true) -> Icons.Outlined.MovieCreation
    file.name.endsWith(".mp3", true) || file.name.endsWith(".wav", true) -> Icons.Outlined.AudioFile
    file.name.endsWith(".pdf", true) -> Icons.Outlined.PictureAsPdf
    file.name.endsWith(".jpg", true) || file.name.endsWith(".png", true) -> Icons.Outlined.Image
    file.name.endsWith(".doc", true) || file.name.endsWith(".docx", true) -> Icons.Outlined.Description
    file.name.endsWith(".zip", true) || file.name.endsWith(".rar", true) -> Icons.Outlined.Description
    else -> Icons.Outlined.Description
}

private fun fileSubtitle(file: LargeFileEntry): String {
    val ext = file.name.substringAfterLast('.', "").uppercase().ifBlank { "FILE" }
    return "$ext · Stored locally"
}

private fun formatSize(gb: Float): String {
    if (gb >= 1f) return "${(gb * 10f).toInt() / 10f} GB"
    val mb = (gb * 1024f).toInt()
    return "$mb MB"
}
