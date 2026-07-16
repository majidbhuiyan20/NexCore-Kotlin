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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.matox.nexcore.core.ui.components.CircularProgressRing
import com.matox.nexcore.domain.model.NexCoreScore
import com.matox.nexcore.domain.model.UserGreeting
import com.matox.nexcore.ui.theme.CardStroke
import com.matox.nexcore.ui.theme.NexCoreGreen
import com.matox.nexcore.ui.theme.Surface
import com.matox.nexcore.ui.theme.TextPrimary
import com.matox.nexcore.ui.theme.TextSecondary

/**
 * Combines the greeting block ("Good Morning, Majid 👋" + status pill)
 * with the NexCore Score circular indicator into a single horizontal
 * card row. Greeting content sits on the left, the score ring on the
 * right — both vertically centered.
 */
@Composable
fun GreetingWithScoreSection(
    greeting: UserGreeting,
    score: NexCoreScore,
    modifier: Modifier = Modifier,
    onInfoClick: () -> Unit = {},
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Surface)
                .border(1.dp, CardStroke, RoundedCornerShape(20.dp))
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // -- Left: greeting text ------------------------------------
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = greeting.greeting,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = greeting.userName,
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 30.sp,
                        ),
                        color = TextPrimary,
                        maxLines = 1,
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "👋",
                        style = MaterialTheme.typography.headlineLarge.copy(fontSize = 26.sp),
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                StatusPill(
                    detail = greeting.detail,
                    subtitle = greeting.subtitle,
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // -- Right: circular NexCore Score indicator ----------------
            ScoreRing(
                score = score,
                onInfoClick = onInfoClick,
            )
        }
    }
}

/**
 * The circular progress ring with a green gradient and "90%" label
 * floating in the middle. Compact ~110dp size to fit beside the
 * greeting text.
 */
@Composable
private fun ScoreRing(
    score: NexCoreScore,
    onInfoClick: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        CircularProgressRing(
            progress = score.value / 100f,
            size = 110.dp,
            strokeWidth = 10.dp,
            progressBrush = Brush.linearGradient(
                colors = listOf(Color(0xFF34D399), NexCoreGreen, Color(0xFF15803D)),
            ),
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = "${score.value}",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 28.sp,
                    ),
                    color = TextPrimary,
                )
                Text(
                    text = "%",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                    ),
                    color = NexCoreGreen,
                )
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = score.label,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                color = TextSecondary,
            )
            Spacer(modifier = Modifier.width(4.dp))
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onInfoClick),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.HelpOutline,
                    contentDescription = "What is NexCore Score?",
                    tint = TextSecondary,
                    modifier = Modifier.size(12.dp),
                )
            }
        }
    }
}

/**
 * Reused from the previous [GreetingSection] — small green-tinted pill
 * showing "Everything looks good / Keep it up!" alongside a sparkle icon.
 */
@Composable
private fun StatusPill(detail: String, subtitle: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(NexCoreGreen.copy(alpha = 0.10f))
            .padding(horizontal = 10.dp, vertical = 8.dp),
    ) {
        Box(
            modifier = Modifier
                .width(3.dp)
                .height(24.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(NexCoreGreen),
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(
                text = detail,
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                color = TextPrimary,
                maxLines = 1,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary,
                maxLines = 1,
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Icon(
            imageVector = Icons.Outlined.AutoAwesome,
            contentDescription = null,
            tint = NexCoreGreen,
            modifier = Modifier
                .size(22.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(NexCoreGreen.copy(alpha = 0.18f))
                .padding(4.dp),
        )
    }
}