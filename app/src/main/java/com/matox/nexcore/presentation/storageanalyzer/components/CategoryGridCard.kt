package com.matox.nexcore.presentation.storageanalyzer.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.AudioFile
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.matox.nexcore.core.ui.components.IconChip
import com.matox.nexcore.core.util.toColor
import com.matox.nexcore.domain.model.StorageCategory
import com.matox.nexcore.ui.theme.CardStroke
import com.matox.nexcore.ui.theme.Surface
import com.matox.nexcore.ui.theme.TextPrimary
import com.matox.nexcore.ui.theme.TextSecondary

/**
 * Card containing the 7-tile category grid (Images / Videos / Apps /
 * Documents / Audio / Downloads / Others). Uses `FlowRow` so the
 * grid wraps gracefully on narrow screens.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CategoryGridCard(
    categories: List<StorageCategory>,
    modifier: Modifier = Modifier,
    onCategoryClick: (StorageCategory) -> Unit = {},
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
                text = "Storage by Category",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = TextPrimary,
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "${categories.size} categories",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary,
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            maxItemsInEachRow = 4,
        ) {
            categories.forEach { category ->
                CategoryTile(
                    category = category,
                    modifier = Modifier
                        .weight(1f, fill = true)
                        .height(112.dp),
                    onClick = { onCategoryClick(category) },
                )
            }
        }
    }
}

@Composable
private fun CategoryTile(
    category: StorageCategory,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val accent = category.accent.toColor()
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF18233C))
            .border(1.dp, CardStroke, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconChip(
                icon = iconForCategory(category.id),
                accent = accent,
                size = 30.dp,
                iconSize = 16.dp,
                backgroundAlpha = 0.22f,
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = category.name,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                color = TextSecondary,
                maxLines = 1,
            )
        }

        Column {
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = formatNumber(category.usedGb),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                    ),
                    color = TextPrimary,
                    maxLines = 1,
                )
                Spacer(modifier = Modifier.width(2.dp))
                Text(
                    text = "GB",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary,
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Canvas(
                    modifier = Modifier
                        .weight(1f)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp)),
                ) {
                    val total = size.width
                    drawRoundRect(
                        color = Color(0xFF1F2A44),
                        cornerRadius = CornerRadius(2f, 2f),
                        size = Size(total, size.height),
                    )
                    drawRoundRect(
                        brush = Brush.horizontalGradient(
                            colors = listOf(accent.copy(alpha = 0.6f), accent),
                        ),
                        cornerRadius = CornerRadius(2f, 2f),
                        size = Size(total * (category.percent / 100f).coerceIn(0f, 1f), size.height),
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "${category.percent}%",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = accent,
                    maxLines = 1,
                )
            }
        }
    }
}

private fun iconForCategory(id: String): ImageVector = when (id) {
    "cat_images" -> Icons.Outlined.Image
    "cat_videos" -> Icons.Outlined.PlayArrow
    "cat_apps" -> Icons.Outlined.Apps
    "cat_documents" -> Icons.Outlined.Description
    "cat_audio" -> Icons.Outlined.AudioFile
    "cat_downloads" -> Icons.Outlined.FileDownload
    "cat_others" -> Icons.Outlined.Folder
    else -> Icons.Outlined.Folder
}

private fun formatNumber(value: Float): String =
    ((value * 10f).toInt() / 10f).toString()
