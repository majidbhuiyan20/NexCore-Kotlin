package com.matox.nexcore.presentation.dashboard.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.matox.nexcore.core.util.headerIcon
import com.matox.nexcore.domain.model.InfoCardData

@Composable
fun InfoCardsRow(
    installedApps: InfoCardData,
    dataUsage: InfoCardData,
    notifications: InfoCardData,
    modifier: Modifier = Modifier,
    onClick: (InfoCardData) -> Unit = {},
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        InfoCardView(
            data = installedApps,
            icon = installedApps.headerIcon(),
            modifier = Modifier.weight(1f),
            onClick = { onClick(installedApps) },
        )
        InfoCardView(
            data = dataUsage,
            icon = dataUsage.headerIcon(),
            showBarChart = true,
            modifier = Modifier.weight(1f),
            onClick = { onClick(dataUsage) },
        )
        InfoCardView(
            data = notifications,
            icon = notifications.headerIcon(),
            modifier = Modifier.weight(1f),
            onClick = { onClick(notifications) },
        )
    }
}