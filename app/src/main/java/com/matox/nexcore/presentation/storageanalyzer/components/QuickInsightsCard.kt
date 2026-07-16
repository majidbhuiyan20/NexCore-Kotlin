package com.matox.nexcore.presentation.storageanalyzer.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FileCopy
import androidx.compose.material.icons.outlined.FolderOff
import androidx.compose.material.icons.outlined.PhotoSizeSelectLarge
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.matox.nexcore.core.ui.components.IconChip
import com.matox.nexcore.core.util.toColor
import com.matox.nexcore.domain.model.MetricAccent
import com.matox.nexcore.domain.model.StorageInsights
import com.matox.nexcore.ui.theme.CardStroke
import com.matox.nexcore.ui.theme.Surface
import com.matox.nexcore.ui.theme.TextPrimary
import com.matox.nexcore.ui.theme.TextSecondary

/**
 * 2x2 grid of quick insight stats: Large Files, Duplicate Files,
 * Empty Folders, Old Files. Each tile shows a small accent icon,
 * the count (large) and the size in GB (muted).
 */
@Composable
fun QuickInsightsCard(
    insights: StorageInsights,
    modifier: Modifier = Modifier,
    onInsightClick: (InsightKind) -> Unit = {},
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(Surface)
            .border(1.dp, CardStroke, RoundedCornerShape(22.dp))
            .padding(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Quick Insights",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = TextPrimary,
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Reclaim space",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary,
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        val rows = listOf(
            listOf(
                InsightSpec(
                    kind = InsightKind.LARGE_FILES,
                    label = "Large Files",
                    icon = Icons.Outlined.PhotoSizeSelectLarge,
                    accent = MetricAccent.PINK,
                    count = insights.largeFilesCount,
                    sizeLabel = "${insights.largeFilesGb} GB",
                ),
                InsightSpec(
                    kind = InsightKind.DUPLICATES,
                    label = "Duplicate Files",
                    icon = Icons.Outlined.FileCopy,
                    accent = MetricAccent.ORANGE,
                    count = insights.duplicateCount,
                    sizeLabel = "${insights.duplicateGb} GB",
                ),
            ),
            listOf(
                InsightSpec(
                    kind = InsightKind.EMPTY_FOLDERS,
                    label = "Empty Folders",
                    icon = Icons.Outlined.FolderOff,
                    accent = MetricAccent.CYAN,
                    count = insights.emptyFolders,
                    sizeLabel = "${insights.emptyFolders * 5} MB",
                ),
                InsightSpec(
                    kind = InsightKind.OLD_FILES,
                    label = "Old Files",
                    icon = Icons.Outlined.Schedule,
                    accent = MetricAccent.VIOLET,
                    count = insights.oldFilesCount,
                    sizeLabel = "${insights.oldFilesGb} GB",
                ),
            ),
        )

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            rows.forEach { rowSpecs ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    rowSpecs.forEach { spec ->
                        InsightTile(
                            spec = spec,
                            modifier = Modifier
                                .weight(1f)
                                .height(96.dp),
                            onClick = { onInsightClick(spec.kind) },
                        )
                    }
                }
            }
        }
    }
}

enum class InsightKind { LARGE_FILES, DUPLICATES, EMPTY_FOLDERS, OLD_FILES }

private data class InsightSpec(
    val kind: InsightKind,
    val label: String,
    val icon: ImageVector,
    val accent: MetricAccent,
    val count: Int,
    val sizeLabel: String,
)

@Composable
private fun InsightTile(
    spec: InsightSpec,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val accent = spec.accent.toColor()
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF18233C))
            .border(1.dp, CardStroke, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconChip(
            icon = spec.icon,
            accent = accent,
            size = 36.dp,
            iconSize = 18.dp,
            backgroundAlpha = 0.22f,
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(
                text = spec.count.toString(),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                ),
                color = TextPrimary,
                maxLines = 1,
            )
            Text(
                text = spec.label,
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary,
                maxLines = 1,
            )
            Text(
                text = spec.sizeLabel,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                color = accent,
                maxLines = 1,
            )
        }
    }
}
