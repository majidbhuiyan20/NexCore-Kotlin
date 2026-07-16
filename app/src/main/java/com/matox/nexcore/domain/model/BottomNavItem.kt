package com.matox.nexcore.domain.model

/**
 * One slot in the bottom navigation bar.
 *
 * - [isCenter] renders as a raised circular FAB and ignores [accent].
 * - [isActive] highlights the label + icon and shows the underline
 *   indicator (currently only Home is active in the mockup).
 */
data class BottomNavItem(
    val id: String,
    val label: String,
    val iconKey: BottomNavIcon,
    val isCenter: Boolean,
    val isActive: Boolean,
)

enum class BottomNavIcon {
    HOME,
    FILES,
    APPS,
    SETTINGS,
}