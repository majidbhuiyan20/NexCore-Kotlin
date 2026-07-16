package com.matox.nexcore.domain.model

/**
 * Storage usage card payload. [breakdown] entries are summed for the
 * donut chart total.
 */
data class StorageUsage(
    val totalUsedGb: Int,
    val breakdown: List<StorageBreakdown>,
) {
    init {
        require(breakdown.isNotEmpty()) { "Storage breakdown must not be empty" }
    }
}

data class StorageBreakdown(
    val category: StorageCategory,
    val sizeGb: Int,
    val accent: MetricAccent,
) {
    init {
        require(sizeGb >= 0) { "sizeGb must be non-negative, got $sizeGb" }
    }
}

enum class StorageCategory(val displayName: String) {
    IMAGES("Images"),
    VIDEOS("Videos"),
    APPS("Apps"),
    DOCUMENTS("Documents"),
    OTHERS("Others"),
}