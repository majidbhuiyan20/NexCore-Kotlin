package com.matox.nexcore.domain.model

/**
 * One tile in the Storage Analyzer category grid.
 *
 * [percent] is the share of total internal storage used by this
 * category, in the range 0..100. [usedGb] is the absolute size.
 */
data class StorageCategory(
    val id: String,
    val name: String,
    val usedGb: Float,
    val percent: Int,
    val accent: MetricAccent,
)
