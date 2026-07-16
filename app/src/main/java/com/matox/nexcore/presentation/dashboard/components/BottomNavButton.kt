package com.matox.nexcore.presentation.dashboard.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.matox.nexcore.core.util.icon
import com.matox.nexcore.domain.model.BottomNavItem
import com.matox.nexcore.ui.theme.NexCoreGreen
import com.matox.nexcore.ui.theme.NexCoreGreenAccent
import com.matox.nexcore.ui.theme.NexCoreGreenDark
import com.matox.nexcore.ui.theme.TextPrimary
import com.matox.nexcore.ui.theme.TextSecondary

/**
 * Renders one slot of the bottom navigation bar.
 *
 *  - [BottomNavItem.isCenter] → big floating action button (raised, gradient)
 *  - [BottomNavItem.isActive] (non-center) → capsule pill background with
 *    bold green label (like a segmented control chip)
 *  - Otherwise → minimal icon + label, both muted
 */
@Composable
fun BottomNavButton(
    item: BottomNavItem,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    when {
        item.isCenter -> CenterFab(
            item = item,
            modifier = modifier,
            onClick = onClick,
        )
        item.isActive -> ActivePillButton(
            item = item,
            modifier = modifier,
            onClick = onClick,
        )
        else -> InactiveButton(
            item = item,
            modifier = modifier,
            onClick = onClick,
        )
    }
}

/** Active item — green-tinted capsule pill with green icon and bold label. */
@Composable
private fun ActivePillButton(
    item: BottomNavItem,
    modifier: Modifier,
    onClick: () -> Unit,
) {
    Column(
        modifier = modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 4.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            NexCoreGreen.copy(alpha = 0.28f),
                            NexCoreGreenAccent.copy(alpha = 0.22f),
                        ),
                    ),
                )
                .border(
                    width = 1.dp,
                    color = NexCoreGreen.copy(alpha = 0.35f),
                    shape = RoundedCornerShape(20.dp),
                )
                .padding(horizontal = 14.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Icon(
                imageVector = item.iconKey.icon(),
                contentDescription = item.label,
                tint = NexCoreGreen,
                modifier = Modifier.size(18.dp),
            )
            Text(
                text = item.label,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = NexCoreGreen,
                maxLines = 1,
            )
        }
        Spacer(modifier = Modifier.height(2.dp))
        // tiny underline accent under the pill
        Box(
            modifier = Modifier
                .size(width = 14.dp, height = 2.dp)
                .clip(CircleShape)
                .background(NexCoreGreen),
        )
    }
}

/** Inactive item — simple icon + label, slate tint. */
@Composable
private fun InactiveButton(
    item: BottomNavItem,
    modifier: Modifier,
    onClick: () -> Unit,
) {
    Column(
        modifier = modifier
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = item.iconKey.icon(),
            contentDescription = item.label,
            tint = TextSecondary,
            modifier = Modifier.size(22.dp),
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = item.label,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
            color = TextSecondary,
            maxLines = 1,
        )
    }
}

/**
 * Center floating action button — gradient fill, pulsing ring,
 * soft drop shadow. Sits 22dp above the bar baseline.
 */
@Composable
private fun CenterFab(
    item: BottomNavItem,
    modifier: Modifier,
    onClick: () -> Unit,
) {
    val infinite = rememberInfiniteTransition(label = "fab-pulse")
    val pulseScale by infinite.animateFloat(
        initialValue = 1f,
        targetValue = 1.18f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "pulse-scale",
    )
    val pulseAlpha by infinite.animateFloat(
        initialValue = 0.55f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "pulse-alpha",
    )

    Box(
        modifier = modifier
            .size(76.dp)
            .offset(y = (-22).dp),
        contentAlignment = Alignment.Center,
    ) {
        // Pulsing halo ring (animated)
        Box(
            modifier = Modifier
                .size(72.dp)
                .scale(pulseScale)
                .clip(CircleShape)
                .background(NexCoreGreen.copy(alpha = pulseAlpha * 0.6f)),
        )

        // Main FAB — green gradient, white icon
        Box(
            modifier = Modifier
                .size(60.dp)
                .shadow(
                    elevation = 14.dp,
                    shape = CircleShape,
                    ambientColor = NexCoreGreen.copy(alpha = 0.55f),
                    spotColor = NexCoreGreen.copy(alpha = 0.55f),
                )
                .clip(CircleShape)
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF4ADE80),
                            NexCoreGreen,
                            NexCoreGreenDark,
                        ),
                    ),
                )
                .border(
                    width = 1.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.35f),
                            Color.White.copy(alpha = 0.05f),
                        ),
                    ),
                    shape = CircleShape,
                )
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = item.iconKey.icon(),
                contentDescription = item.label,
                tint = TextPrimary,
                modifier = Modifier.size(26.dp),
            )
        }
    }
}