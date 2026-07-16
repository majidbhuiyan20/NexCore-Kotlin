package com.matox.nexcore.domain.model

/**
 * Cheap-to-compute rollups used by the Storage Analyzer "Quick
 * Insights" card. These are estimates — a deeper scan would
 * require explicit MANAGE_EXTERNAL_STORAGE, which the user opted
 * out of in favour of a friendlier permission surface.
 */
data class StorageInsights(
    val largeFilesCount: Int,
    val largeFilesGb: Float,
    val duplicateCount: Int,
    val duplicateGb: Float,
    val emptyFolders: Int,
    val oldFilesCount: Int,
    val oldFilesGb: Float,
)
