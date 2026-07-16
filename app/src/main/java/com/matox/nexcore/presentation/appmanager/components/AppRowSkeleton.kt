package com.matox.nexcore.presentation.appmanager.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.matox.nexcore.ui.theme.CardStroke
import com.matox.nexcore.ui.theme.Surface

/**
 * Animated shimmer skeleton that mirrors the visual layout of
 * [AppListItem] — same heights, same border, same icon + text
 * shapes. Rendered as a placeholder while the disk cache or the
 * PackageManager snapshot is still in flight.
 *
 * The shimmer is driven by a single infinite transition that shifts
 * a horizontal gradient across the row; lighter than the typical
 * per-element Modifier.alpha animation and looks identical to the
 * Material 3 placeholder.
 */
@Composable
fun AppRowSkeleton(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val xShift by transition.animateFloat(
        initialValue = -1f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200),
            repeatMode = RepeatMode.Restart,
        ),
        label = "shimmer-x",
    )

    val base = Color(0xFF1A2540)
    val highlight = Color(0xFF2A3A60)
    val shimmerBrush = Brush.linearGradient(
        colors = listOf(base, highlight, base),
        start = Offset(xShift * 400f, 0f),
        end = Offset((xShift + 1f) * 400f, 0f),
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Surface)
            .border(1.dp, CardStroke, RoundedCornerShape(18.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Icon skeleton
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(shimmerBrush),
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Box(
                modifier = Modifier
                    .height(14.dp)
                    .fillMaxWidth(0.55f)
                    .clip(RoundedCornerShape(4.dp))
                    .background(shimmerBrush),
            )
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .height(10.dp)
                    .fillMaxWidth(0.75f)
                    .clip(RoundedCornerShape(4.dp))
                    .background(shimmerBrush),
            )
            Spacer(modifier = Modifier.height(6.dp))
            Box(
                modifier = Modifier
                    .height(10.dp)
                    .fillMaxWidth(0.6f)
                    .clip(RoundedCornerShape(4.dp))
                    .background(shimmerBrush),
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        // Right-side pill + action button skeletons
        Box(
            modifier = Modifier
                .size(width = 56.dp, height = 22.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(shimmerBrush),
        )
    }
}

/** Convenience: render N skeleton rows vertically stacked. */
@Composable
fun AppListSkeleton(
    count: Int = 8,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(10.dp),
    ) {
        repeat(count) {
            AppRowSkeleton()
        }
    }
}
