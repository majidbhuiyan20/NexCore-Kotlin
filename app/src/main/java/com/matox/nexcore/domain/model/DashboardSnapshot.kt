package com.matox.nexcore.domain.model

/**
 * Aggregated dashboard payload. This is what the UI consumes in one shot.
 */
data class DashboardSnapshot(
    val greeting: UserGreeting,
    val nexCoreScore: NexCoreScore,
    val metrics: List<SystemMetric>,
)