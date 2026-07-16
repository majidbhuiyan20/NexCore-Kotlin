package com.matox.nexcore.presentation.dashboard

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.matox.nexcore.domain.model.DashboardSnapshot
import com.matox.nexcore.domain.model.MetricAccent
import com.matox.nexcore.domain.model.MetricType
import com.matox.nexcore.domain.model.NexCoreScore
import com.matox.nexcore.domain.model.ScoreStatus
import com.matox.nexcore.domain.model.SystemMetric
import com.matox.nexcore.domain.model.UserGreeting
import com.matox.nexcore.presentation.dashboard.state.DashboardUiState
import com.matox.nexcore.ui.theme.NexCoreTheme

private val PreviewSnapshot = DashboardSnapshot(
    greeting = UserGreeting(
        userName = "Majid",
        greeting = "Good Morning",
        tagline = "Your device, Under Control.",
        detail = "Everything looks good",
        subtitle = "Keep it up!",
    ),
    nexCoreScore = NexCoreScore(
        value = 94,
        label = "NexCore Score",
        status = ScoreStatus.Excellent,
    ),
    metrics = listOf(
        SystemMetric(MetricType.RAM, "RAM", 62f, "4.1 GB", "/ 8 GB", MetricAccent.BLUE),
        SystemMetric(MetricType.STORAGE, "Storage", 48f, "123 GB", "/ 256 GB", MetricAccent.PURPLE),
        SystemMetric(MetricType.BATTERY, "Battery", 82f, "82%", "Charging", MetricAccent.GREEN),
        SystemMetric(MetricType.CPU, "CPU", 22f, "22%", "1.2 GHz", MetricAccent.ORANGE),
        SystemMetric(MetricType.TEMPERATURE, "Temp", 33f, "33°C", "Normal", MetricAccent.RED),
    ),
)

@Preview(showBackground = true, backgroundColor = 0xFF0B1220, widthDp = 412, heightDp = 920)
@Composable
fun DashboardPreview() {
    NexCoreTheme {
        DashboardContent(
            state = DashboardUiState.Success(PreviewSnapshot),
            modifier = Modifier,
            onMenuClick = {},
            onSearchClick = {},
            onNotificationsClick = {},
            onInfoClick = {},
            onMetricClick = {},
        )
    }
}