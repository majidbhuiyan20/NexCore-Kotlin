package com.matox.nexcore.presentation.dashboard.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.matox.nexcore.core.ui.components.IconChip
import com.matox.nexcore.core.util.toColor
import com.matox.nexcore.domain.model.InfoCardData
import com.matox.nexcore.ui.theme.CardStroke
import com.matox.nexcore.ui.theme.Surface
import com.matox.nexcore.ui.theme.TextPrimary
import com.matox.nexcore.ui.theme.TextSecondary

@Composable
fun InfoCardView(
    data: InfoCardData,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    showBarChart: Boolean = false,
    onClick: () -> Unit = {},
) {
    val accent = data.accent.toColor()
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(Surface)
            .border(1.dp, CardStroke, RoundedCornerShape(18.dp))
            .clickable(enabled = data.showChevron, onClick = onClick)
            .padding(12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconChip(
                icon = icon,
                accent = accent,
                size = 28.dp,
                iconSize = 14.dp,
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = data.title,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                color = TextSecondary,
            )
        }
        Spacer(modifier = Modifier.height(8.dp))

        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = data.bigValue,
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = TextPrimary,
            )
            if (data.unit != null) {
                Spacer(modifier = Modifier.width(2.dp))
                Text(
                    text = data.unit,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = TextSecondary,
                    modifier = Modifier.padding(bottom = 4.dp),
                )
            }
        }
        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = data.footnotePrimary,
            style = MaterialTheme.typography.bodySmall,
            color = TextPrimary,
        )
        if (data.footnoteSecondary != null) {
            Text(
                text = data.footnoteSecondary,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
            )
        }

        if (data.showChevron) {
            Spacer(modifier = Modifier.height(2.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                    contentDescription = null,
                    tint = TextSecondary,
                    modifier = Modifier.size(18.dp),
                )
            }
        } else if (showBarChart) {
            DataUsageBars(modifier = Modifier
                .fillMaxWidth()
                .height(34.dp))
        }
    }
}

/**
 * Mini bar chart strip used in the Data Usage card. Heights are
 * generated deterministically so the preview matches across runs.
 */
@Composable
private fun DataUsageBars(modifier: Modifier = Modifier) {
    val bars = listOf(0.4f, 0.55f, 0.35f, 0.7f, 0.5f, 0.85f, 0.6f, 0.95f, 0.7f, 0.8f, 0.55f, 0.65f)
    val color = Color(0xFF22D3EE)
    Box(modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val barCount = bars.size
            val gap = 2f
            val barW = (this.size.width - gap * (barCount - 1)) / barCount
            bars.forEachIndexed { i, h ->
                val barH = this.size.height * h
                drawRoundRect(
                    color = color,
                    topLeft = Offset(x = i * (barW + gap), y = this.size.height - barH),
                    size = Size(barW, barH),
                    cornerRadius = CornerRadius(2f, 2f),
                )
            }
        }
    }
}