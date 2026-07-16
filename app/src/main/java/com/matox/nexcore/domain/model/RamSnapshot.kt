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
 * - [recentEvents] — synthesised timeline entries (new app seen,
 *   large allocation, kernel reclaim, etc). Most-recent first,
 *   capped to 20 entries.
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
    val recentEvents: List<MemoryEvent> = emptyList(),
) {
    /** Convenience — bytes free for the hero card's "Available" label. */
    val availableGb: Float
        get() = (totalGb - usedGb).coerceAtLeast(0f)
}

/**
 * One row in the RAM detail screen's "Top apps by memory" list.
 *
 * - [pssMb] — proportional set size, the most realistic single
 *   number for an app's memory footprint. When the process is
 *   invisible to the Android sandbox, this falls back to the app's
 *   on-disk footprint (APK + dataDir) as a stable proxy.
 * - [privateDirtyMb] — RAM that would be freed if the process died.
 * - [isSystem] — true for OS-installed packages; surfaced as a small
 *   badge in the row.
 * - [hasRealPss] — true only when the figure came from the live
 *   `ActivityManager.getProcessMemoryInfo` PSS readout. Used by the
 *   UI to show the "Live" pulsing dot on foreground / visible apps
 *   vs. the on-disk proxy used for background apps.
 */
data class AppRamUsage(
    val packageName: String,
    val displayName: String,
    val pssMb: Long,
    val privateDirtyMb: Long,
    val isSystem: Boolean,
    val hasRealPss: Boolean = false,
)