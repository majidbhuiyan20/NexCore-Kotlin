package com.matox.nexcore.domain.model

/**
 * Domain model representing the aggregated "NexCore Score"
 * shown in the prominent circular indicator on the dashboard.
 */
data class NexCoreScore(
    val value: Int,
    val label: String,
    val status: ScoreStatus,
) {
    init {
        require(value in 0..100) { "score value must be in 0..100, got $value" }
    }
}

enum class ScoreStatus(val displayName: String) {
    Excellent("Excellent"),
    Good("Good"),
    Fair("Fair"),
    Poor("Poor"),
}