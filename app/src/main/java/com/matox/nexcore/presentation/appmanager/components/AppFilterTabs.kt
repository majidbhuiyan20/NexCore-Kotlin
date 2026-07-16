package com.matox.nexcore.presentation.appmanager.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.matox.nexcore.domain.model.AppFilterTab
import com.matox.nexcore.ui.theme.NexCoreGreen
import com.matox.nexcore.ui.theme.TextPrimary
import com.matox.nexcore.ui.theme.TextSecondary

/**
 * Filter tab row: All Apps / User Apps / System Apps. The active
 * tab gets bold text and a green underline accent.
 */
@Composable
fun AppFilterTabs(
    selected: AppFilterTab,
    modifier: Modifier = Modifier,
    onTabSelected: (AppFilterTab) -> Unit = {},
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween,
    ) {
        AppFilterTab.entries.forEach { tab ->
            TabButton(
                tab = tab,
                isActive = selected == tab,
                onClick = { onTabSelected(tab) },
            )
        }
    }
}

@Composable
private fun TabButton(
    tab: AppFilterTab,
    isActive: Boolean,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = tab.label,
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
            ),
            color = if (isActive) TextPrimary else TextSecondary,
        )
        Spacer(modifier = Modifier.height(8.dp))
        if (isActive) {
            Box(
                modifier = Modifier
                    .width(40.dp)
                    .height(3.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(Color.Transparent, NexCoreGreen, Color.Transparent),
                        ),
                    ),
            )
        } else {
            Spacer(modifier = Modifier.height(3.dp))
        }
    }
}
