package com.matox.nexcore.presentation.dashboard.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.matox.nexcore.domain.model.BottomNavItem
import com.matox.nexcore.ui.theme.BackgroundGradientBottom
import com.matox.nexcore.ui.theme.NexCoreGreen
import com.matox.nexcore.ui.theme.Surface
import com.matox.nexcore.ui.theme.SurfaceVariant

/**
 * Bottom navigation "floating dock".
 *
 * Renders a horizontally-margined rounded card with:
 *  - subtle top-edge green glow
 *  - soft drop shadow (visible above the system gesture bar)
 *  - centered Row with one slot per [BottomNavItem]
 *
 * The dock is expected to be placed **fixed** at the bottom of the
 * screen with [navigationBarsPadding] for safe-area respect. The center
 * FAB (`isCenter = true`) overlaps upward on its own.
 */
@Composable
fun DashboardBottomBar(
    items: List<BottomNavItem>,
    modifier: Modifier = Modifier,
    height: Dp = 76.dp,
    onItemClick: (BottomNavItem) -> Unit = {},
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        contentAlignment = Alignment.BottomCenter,
    ) {
        // Main dock surface with drop shadow
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(height)
                .shadow(
                    elevation = 24.dp,
                    shape = RoundedCornerShape(28.dp),
                    ambientColor = NexCoreGreen.copy(alpha = 0.25f),
                    spotColor = Color.Black.copy(alpha = 0.45f),
                )
                .clip(RoundedCornerShape(28.dp))
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            SurfaceVariant,
                            Surface,
                        ),
                    ),
                ),
            contentAlignment = Alignment.Center,
        ) {
            // Top-edge green glow line
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.5.dp)
                    .align(Alignment.TopCenter)
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                NexCoreGreen.copy(alpha = 0.0f),
                                NexCoreGreen.copy(alpha = 0.7f),
                                NexCoreGreen.copy(alpha = 0.0f),
                            ),
                        ),
                    ),
            )

            // Bottom subtle gradient fade
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .align(Alignment.BottomCenter)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                BackgroundGradientBottom.copy(alpha = 0.4f),
                            ),
                        ),
                    ),
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                items.forEach { item ->
                    BottomNavButton(
                        item = item,
                        modifier = Modifier.weight(1f),
                        onClick = { onItemClick(item) },
                    )
                }
            }
        }
    }
}