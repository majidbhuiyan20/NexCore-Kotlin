package com.matox.nexcore.core.util

import androidx.compose.ui.graphics.Color
import com.matox.nexcore.domain.model.MetricAccent
import com.matox.nexcore.ui.theme.MetricBlue
import com.matox.nexcore.ui.theme.MetricCyan
import com.matox.nexcore.ui.theme.MetricGreen
import com.matox.nexcore.ui.theme.MetricOrange
import com.matox.nexcore.ui.theme.MetricPink
import com.matox.nexcore.ui.theme.MetricPurple
import com.matox.nexcore.ui.theme.MetricRed
import com.matox.nexcore.ui.theme.MetricTeal
import com.matox.nexcore.ui.theme.MetricViolet

/**
 * Maps semantic [MetricAccent] enum values to actual Compose colors.
 * Keeps Compose types out of the domain layer.
 */
fun MetricAccent.toColor(): Color = when (this) {
    MetricAccent.BLUE -> MetricBlue
    MetricAccent.CYAN -> MetricCyan
    MetricAccent.PURPLE -> MetricPurple
    MetricAccent.VIOLET -> MetricViolet
    MetricAccent.GREEN -> MetricGreen
    MetricAccent.ORANGE -> MetricOrange
    MetricAccent.RED -> MetricRed
    MetricAccent.PINK -> MetricPink
    MetricAccent.TEAL -> MetricTeal
}