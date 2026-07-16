package com.matox.nexcore.domain.model

/**
 * A point-in-time snapshot of device memory state.
 *
 * - [usedGb] / [totalGb] / [percent] — derived from
 *   [android.app.ActivityManager.MemoryInfo].
 * - [lowMemory] — true when the device has crossed the system
 *   `lowMemory` threshold.
 * - [cachedMb] / [buffersMb] / [activeMb] / [inactiveMb] / [swapTotalMb] /
 *   [swapFreeMb] — parsed from `/proc/meminfo`.
 * - [historyPercent] — rolling buffer of recent RAM usage percentages
 *   (oldest first → newest last). Used by the detail screen's
 *   time-series chart.
 * - [topApps] — top RAM consumers by PSS, capped to 8 rows.
 */
data class RamSnapshot(
    val usedGb: Float,
    val totalGb: Float,
    val percent: Int,
    val lowMemory: Boolean,
    val thresholdGb: Float,
    val cachedMb: Long,
    val buffersMb: Long,
    val activeMb: Long,
    val inactiveMb: Long,
    val swapTotalMb: Long,
    val swapFreeMb: Long,
    val historyPercent: List<Int>,
    val topApps: List<AppRamUsage>,
) {
    /** Convenience — bytes free for the hero card's "Available" label. */
    val availableGb: Float
        get() = (totalGb - usedGb).coerceAtLeast(0f)
}

/**
 * One row in the RAM detail screen's "Top apps by memory" list.
 *
 * - [pssMb] — proportional set size, the most realistic single
 *   number for an app's memory footprint.
 * - [privateDirtyMb] — RAM that would be freed if the process died.
 * - [isSystem] — true for OS-installed packages; surfaced as a small
 *   badge in the row.
 */
data class AppRamUsage(
    val packageName: String,
    val displayName: String,
    val pssMb: Long,
    val privateDirtyMb: Long,
    val isSystem: Boolean,
)