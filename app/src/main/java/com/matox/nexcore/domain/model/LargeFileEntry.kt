package com.matox.nexcore.domain.model

/**
 * Single entry in the "Top Large Files" list on the Storage
 * Analyzer screen. [isApk] is surfaced so the UI can pick a
 * slightly different icon style for installable packages.
 */
data class LargeFileEntry(
    val id: String,
    val name: String,
    val sizeGb: Float,
    val accent: MetricAccent,
    val isApk: Boolean = false,
)
