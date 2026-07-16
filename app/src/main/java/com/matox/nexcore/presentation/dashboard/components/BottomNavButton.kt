package com.matox.nexcore.presentation.dashboard.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.matox.nexcore.core.util.icon
import com.matox.nexcore.domain.model.BottomNavItem
import com.matox.nexcore.ui.theme.NexCoreGreen
import com.matox.nexcore.ui.theme.Surface
import com.matox.nexcore.ui.theme.TextSecondary

@Composable
fun BottomNavButton(
    item: BottomNavItem,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    if (item.isCenter) {
        CenterFab(
            item = item,
            modifier = modifier.offset(y = (-14).dp),
            onClick = onClick,
        )
        return
    }

    val iconTint = if (item.isActive) NexCoreGreen else TextSecondary
    val labelColor = if (item.isActive) NexCoreGreen else TextSecondary

    Column(
        modifier = modifier
            .clickable(onClick = onClick)
            .offset(y = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier.size(40.dp),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = item.iconKey.icon(),
                contentDescription = item.label,
                tint = iconTint,
                modifier = Modifier.size(22.dp),
            )
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = item.label,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
            color = labelColor,
        )
        // Active underline indicator (Home in mockup)
        if (item.isActive) {
            Spacer(modifier = Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .size(width = 18.dp, height = 3.dp)
                    .clip(CircleShape)
                    .background(NexCoreGreen),
            )
        }
    }
}

@Composable
private fun CenterFab(
    item: BottomNavItem,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    Box(
        modifier = modifier
            .size(64.dp)
            .clip(CircleShape)
            .background(Surface)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        // Pulse ring
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(NexCoreGreen.copy(alpha = 0.18f)),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF0B1220)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = item.iconKey.icon(),
                    contentDescription = item.label,
                    tint = NexCoreGreen,
                    modifier = Modifier.size(22.dp),
                )
            }
        }
    }
}