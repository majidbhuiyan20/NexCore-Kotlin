package com.matox.nexcore.domain.model

/**
 * Generic data for the three info cards (Installed Apps / Data Usage /
 * Notifications). Each card shows an icon chip, a title, a big number
 * with optional unit, and one or two lines of footnote text.
 */
data class InfoCardData(
    val id: String,
    val title: String,
    val bigValue: String,
    val unit: String?,
    val footnotePrimary: String,
    val footnoteSecondary: String?,
    val accent: MetricAccent,
    val showChevron: Boolean,
)