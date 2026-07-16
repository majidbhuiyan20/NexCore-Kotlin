package com.matox.nexcore.data.datasource

import com.matox.nexcore.domain.model.DashboardSnapshot
import com.matox.nexcore.domain.model.MetricAccent
import com.matox.nexcore.domain.model.MetricType
import com.matox.nexcore.domain.model.NexCoreScore
import com.matox.nexcore.domain.model.ScoreStatus
import com.matox.nexcore.domain.model.SystemMetric
import com.matox.nexcore.domain.model.UserGreeting
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Local data source that emits a static dashboard snapshot.
 * In a real app this would aggregate signals from system services,
 * battery manager, storage stats, etc.
 */
interface DashboardLocalDataSource {
    fun snapshot(): Flow<DashboardSnapshot>
}

class FakeDashboardLocalDataSource : DashboardLocalDataSource {

    override fun snapshot(): Flow<DashboardSnapshot> = flow {
        emit(buildSnapshot())
    }

    private fun buildSnapshot(): DashboardSnapshot = DashboardSnapshot(
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
            SystemMetric(
                id = MetricType.RAM,
                label = "RAM",
                valuePercent = 62f,
                primaryValue = "4.1 GB",
                secondaryValue = "/ 8 GB",
                accent = MetricAccent.BLUE,
            ),
            SystemMetric(
                id = MetricType.STORAGE,
                label = "Storage",
                valuePercent = 48f,
                primaryValue = "123 GB",
                secondaryValue = "/ 256 GB",
                accent = MetricAccent.PURPLE,
            ),
            SystemMetric(
                id = MetricType.BATTERY,
                label = "Battery",
                valuePercent = 82f,
                primaryValue = "82%",
                secondaryValue = "Charging",
                accent = MetricAccent.GREEN,
            ),
            SystemMetric(
                id = MetricType.CPU,
                label = "CPU",
                valuePercent = 22f,
                primaryValue = "22%",
                secondaryValue = "1.2 GHz",
                accent = MetricAccent.ORANGE,
            ),
            SystemMetric(
                id = MetricType.TEMPERATURE,
                label = "Temp",
                valuePercent = 33f,
                primaryValue = "33°C",
                secondaryValue = "Normal",
                accent = MetricAccent.RED,
            ),
        ),
    )
}