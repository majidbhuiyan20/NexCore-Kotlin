package com.matox.nexcore.presentation.phoneinfo.components

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.matox.nexcore.core.ui.components.IconChip
import com.matox.nexcore.core.util.toColor
import com.matox.nexcore.domain.model.MetricAccent
import com.matox.nexcore.ui.theme.Surface
import com.matox.nexcore.ui.theme.TextPrimary
import com.matox.nexcore.ui.theme.TextSecondary

/**
 * Generic full-width section card used by Basic Info / Hardware /
 * OS Information / Network.
 *
 *  ┌──────────────────────────────────┐
 *  │ [icon]  Title                    │
 *  │ ──────────────────────────────── │
 *  │  Label                       Value│
 *  │  Label                       Value│
 *  │              ...                 │
 *  └──────────────────────────────────┘
 *
 * Renders one row per `(label, value)` pair. Rows are separated by a
 * thin divider. The whole card is a single column, and the card itself
 * is laid out as one full-width tile in the screen's vertical stack —
 * i.e. NO two-column layout anywhere on the screen.
 */
@Composable
fun SectionCard(
    title: String,
    icon: ImageVector,
    accent: MetricAccent,
    rows: List<Pair<String, String>>,
    modifier: Modifier = Modifier,
) {
    val accentColor = accent.toColor()
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Surface)
            // Border uses the accent color at low alpha so each
            // section card carries its own color identity without
            // overwhelming the dark surface.
            .border(1.dp, accentColor.copy(alpha = 0.45f), RoundedCornerShape(20.dp))
            .padding(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconChip(
                icon = icon,
                accent = accentColor,
                size = 36.dp,
                iconSize = 18.dp,
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = TextPrimary,
            )
        }

        Spacer(modifier = Modifier.size(12.dp))

        rows.forEachIndexed { index, (label, value) ->
            InfoRow(label = label, value = value)
            if (index < rows.lastIndex) {
                Spacer(modifier = Modifier.size(10.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(accentColor.copy(alpha = 0.18f)),
                )
                Spacer(modifier = Modifier.size(10.dp))
            }
        }
    }
}

@Composable
private fun InfoRow(
    label: String,
    value: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            modifier = Modifier
                .weight(1f)
                .padding(end = 12.dp),
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            color = TextPrimary,
            maxLines = 2,
            modifier = Modifier.weight(1.2f),
        )
    }
}
