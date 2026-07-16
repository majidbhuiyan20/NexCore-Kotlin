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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
 * Unified design — every item renders as the **same capsule pill**
 * (same shape, same icon size, same label style). The only thing that
 * varies between active and inactive items is the tint intensity:
 *
 *  - **active**: bright green gradient pill, green icon, bold green
 *    label, and a tiny dot indicator beneath the pill.
 *  - **inactive**: muted slate-grey gradient pill, slate icon, medium
 *    label — still clearly a pill (so the row looks visually unified),
 *    just toned down.
 *
 * The legacy [BottomNavItem.isCenter] flag is preserved for callers
 * that still set it (the field is in the domain model and shared
 * across screens) but is **ignored visually** — the center item gets
 * the same pill treatment as every other slot. A subtle outer halo
 * stays on the center pill so it doesn't completely lose its visual
 * identity.
 */
@Composable
fun BottomNavButton(
    item: BottomNavItem,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    UnifiedPillButton(
        item = item,
        modifier = modifier,
        onClick = onClick,
    )
}

/**
 * One unified pill — used for every item regardless of [BottomNavItem.isCenter]
 * or [BottomNavItem.isActive].
 *
 * Layout (identical for every item):
 *  - Outer Box sized to the dock slot.
 *  - Pill: rounded 22 dp capsule, 14 dp horizontal padding, 8 dp vertical.
 *  - Inside: icon (18 dp) + label (labelMedium), both tinted per state.
 *  - Beneath the pill: small dot indicator (active = green dot,
 *    inactive = transparent) — keeps the dock visually balanced even
 *    when no item is active.
 *  - Center items get a soft animated halo around the pill so the
 *    "Apps" tab still feels slightly elevated without changing the
 *    overall shape.
 */
@Composable
private fun UnifiedPillButton(
    item: BottomNavItem,
    modifier: Modifier,
    onClick: () -> Unit,
) {
    val active = item.isActive

    val pillTint: Color = if (active) NexCoreGreen else TextSecondary
    val pillBgBrush: Brush = if (active) {
        Brush.horizontalGradient(
            colors = listOf(
                NexCoreGreen.copy(alpha = 0.28f),
                NexCoreGreenAccent.copy(alpha = 0.22f),
            ),
        )
    } else {
        Brush.horizontalGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.06f),
                Color.White.copy(alpha = 0.03f),
            ),
        )
    }
    val pillBorder: Color = if (active) {
        NexCoreGreen.copy(alpha = 0.35f)
    } else {
        Color.White.copy(alpha = 0.10f)
    }
    val labelWeight = if (active) FontWeight.Bold else FontWeight.Medium
    val labelColor = pillTint
    val iconSize = 18.dp

    // Soft halo only on the center pill — keeps the "Apps" slot
    // visually distinct without changing the overall pill shape.
    val halo: @Composable (Modifier) -> Unit = { inner ->
        if (item.isCenter) {
            val infinite = rememberInfiniteTransition(label = "pill-halo")
            val haloScale by infinite.animateFloat(
                initialValue = 1f,
                targetValue = 1.08f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 1800, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse,
                ),
                label = "halo-scale",
            )
            val haloAlpha by infinite.animateFloat(
                initialValue = 0.20f,
                targetValue = 0.0f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 1800, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse,
                ),
                label = "halo-alpha",
            )
            Box(
                modifier = inner
                    .scale(haloScale)
                    .clip(RoundedCornerShape(26.dp))
                    .background(NexCoreGreen.copy(alpha = haloAlpha)),
            )
        }
    }

    Column(
        modifier = modifier
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center,
        ) {
            // Halo layer (center only) — drawn behind the pill.
            Box(
                modifier = Modifier
                    .size(width = 70.dp, height = 38.dp),
                contentAlignment = Alignment.Center,
            ) {
                halo(Modifier.size(width = 70.dp, height = 38.dp))
            }

            Row(
                modifier = Modifier
                    .shadow(
                        elevation = if (active) 10.dp else 4.dp,
                        shape = RoundedCornerShape(22.dp),
                        ambientColor = if (active) NexCoreGreen.copy(alpha = 0.30f) else Color.Black.copy(alpha = 0.30f),
                        spotColor = if (active) NexCoreGreen.copy(alpha = 0.30f) else Color.Black.copy(alpha = 0.45f),
                    )
                    .clip(RoundedCornerShape(22.dp))
                    .background(pillBgBrush)
                    .border(
                        width = 1.dp,
                        color = pillBorder,
                        shape = RoundedCornerShape(22.dp),
                    )
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Icon(
                    imageVector = item.iconKey.icon(),
                    contentDescription = item.label,
                    tint = pillTint,
                    modifier = Modifier.size(iconSize),
                )
                Text(
                    text = item.label,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = labelWeight),
                    color = labelColor,
                    maxLines = 1,
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Tiny dot indicator — visible only when active. Same x-position
        // as the center of the pill above.
        Box(
            modifier = Modifier
                .size(width = 14.dp, height = 2.dp)
                .clip(CircleShape)
                .background(if (active) NexCoreGreen else Color.Transparent),
        )
    }
}

// Keep colors referenced so the imports stay intentional.
@Suppress("unused")
private val _keepColors: Pair<Color, Color> = NexCoreGreen to NexCoreGreenDark