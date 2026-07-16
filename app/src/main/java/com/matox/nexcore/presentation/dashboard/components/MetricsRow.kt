package com.matox.nexcore.presentation.dashboard.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.matox.nexcore.domain.model.SystemMetric

@Composable
fun MetricsRow(
    metrics: List<SystemMetric>,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp),
    onMetricClick: (SystemMetric) -> Unit = {},
) {
    LazyRow(
        modifier = modifier,
        contentPadding = contentPadding,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        items(items = metrics, key = { it.id.name }) { metric ->
            MetricCard(
                metric = metric,
                onClick = { onMetricClick(metric) },
            )
        }
    }
}