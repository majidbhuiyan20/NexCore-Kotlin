package com.matox.nexcore.presentation.dashboard.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.matox.nexcore.domain.model.QuickAction
import com.matox.nexcore.ui.theme.NexCoreGreen
import com.matox.nexcore.ui.theme.TextPrimary

@Composable
fun QuickActionsSection(
    actions: List<QuickAction>,
    modifier: Modifier = Modifier,
    onEditClick: () -> Unit = {},
    onActionClick: (QuickAction) -> Unit = {},
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Quick Actions",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = TextPrimary,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = "Edit",
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                color = NexCoreGreen,
                modifier = Modifier
                    .clickable(onClick = onEditClick)
                    .padding(vertical = 4.dp),
            )
            Spacer(modifier = Modifier.size(4.dp))
            Icon(
                imageVector = Icons.Outlined.Edit,
                contentDescription = "Edit quick actions",
                tint = NexCoreGreen,
                modifier = Modifier.size(14.dp),
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Split actions into rows of 5 (10 actions => 2 rows).
        val rows = actions.chunked(5)
        rows.forEachIndexed { rowIndex, rowItems ->
            if (rowIndex > 0) Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                rowItems.forEach { action ->
                    QuickActionTile(
                        action = action,
                        modifier = Modifier.weight(1f),
                        onClick = { onActionClick(action) },
                    )
                }
            }
        }
    }
}