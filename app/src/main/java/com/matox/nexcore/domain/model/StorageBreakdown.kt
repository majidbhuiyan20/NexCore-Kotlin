package com.matox.nexcore.domain.model

/**
 * Full Storage Analyzer payload — internal storage totals,
 * category breakdown, top large files, and rollup insights.
 */
data class StorageBreakdown(
    val internalUsedGb: Float,
    val internalTotalGb: Float,
    val categories: List<StorageCategory>,
    val largeFiles: List<LargeFileEntry>,
    val insights: StorageInsights,
) {
    val internalFreeGb: Float get() = (internalTotalGb - internalUsedGb).coerceAtLeast(0f)
    val usedPercent: Int
        get() = if (internalTotalGb <= 0f) 0
        else ((internalUsedGb / internalTotalGb) * 100f).toInt().coerceIn(0, 100)
}
