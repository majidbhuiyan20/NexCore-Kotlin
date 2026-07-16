package com.matox.nexcore.presentation.dashboard.components

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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.matox.nexcore.core.ui.components.CircularProgressRing
import com.matox.nexcore.core.util.icon
import com.matox.nexcore.core.util.toColor
import com.matox.nexcore.domain.model.SystemMetric
import com.matox.nexcore.ui.theme.CardStroke
import com.matox.nexcore.ui.theme.Surface
import com.matox.nexcore.ui.theme.TextPrimary
import com.matox.nexcore.ui.theme.TextSecondary

@Composable
fun MetricCard(
    metric: SystemMetric,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    val accent = metric.accent.toColor()

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(Surface)
            .border(1.dp, CardStroke, RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start,
        ) {
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(accent.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = metric.id.icon(),
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(13.dp),
                )
            }
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = metric.label,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                color = TextPrimary,
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Box(
            modifier = Modifier.size(78.dp),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressRing(
                progress = metric.valuePercent / 100f,
                size = 78.dp,
                strokeWidth = 6.dp,
                progressBrush = Brush.linearGradient(
                    colors = listOf(accent.copy(alpha = 0.7f), accent),
                ),
            )
            Text(
                text = "${metric.valuePercent.toInt()}%",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = TextPrimary,
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = metric.primaryValue,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
            color = TextPrimary,
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = metric.secondaryValue,
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary,
        )
    }
}
