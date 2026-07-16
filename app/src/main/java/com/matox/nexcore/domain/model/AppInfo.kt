package com.matox.nexcore.domain.model

import android.graphics.Bitmap

/**
 * One row in the App Manager list.
 *
 * - [iconRef] is either a [Loaded] bitmap (the real installed icon
 *   rendered by PackageManager) or [Pending] when the data source
 *   has not yet loaded the icon. Presentation swaps a placeholder
 *   in for [Pending].
 * - [sizeBytes] is the size of the APK on disk (best-effort;
 *   split-APK packages may report 0).
 * - [lastUpdatedEpochMs] comes from `PackageInfo.lastUpdateTime`;
 *   we surface it as "Updated on …" rather than "Installed on …"
 *   because the first-install time isn't always reliable.
 */
data class AppInfo(
    val packageName: String,
    val displayName: String,
    val versionName: String,
    val categoryLabel: String,
    val sizeBytes: Long,
    val lastUpdatedEpochMs: Long,
    val isSystem: Boolean,
    val hasLauncher: Boolean,
    val iconRef: AppIconRef,
)

sealed class AppIconRef {
    data class Loaded(val bitmap: Bitmap) : AppIconRef()
    data object Pending : AppIconRef()
    data object Failed : AppIconRef()
}

/**
 * Cheap, icon-free rollup of installed apps — used by the home
 * dashboard's "Installed Apps" card so we don't have to load
 * icons on every 3 s poll.
 */
data class InstalledAppsCount(
    val total: Int,
    val user: Int,
    val system: Int,
    val totalSizeBytes: Long,
)
