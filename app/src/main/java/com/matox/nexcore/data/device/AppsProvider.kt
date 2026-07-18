package com.matox.nexcore.data.device

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import androidx.core.graphics.drawable.toBitmap
import com.matox.nexcore.domain.model.AppIconRef
import com.matox.nexcore.domain.model.AppInfo
import com.matox.nexcore.domain.model.InstalledAppsCount
import java.io.File

/**
 * Reads installed-application data from Android's PackageManager.
 *
 * Two read paths:
 *  - [snapshot] — full payload including decoded icons. Used by
 *    the App Manager list. Expensive (one Bitmap per app); call
 *    only when the list is actually rendered.
 *  - [count] — cheap icon-free rollup. Used by the home dashboard
 *    on every poll cycle.
 *
 * Icon size is clamped to 96×96 px to keep memory bounded; ~300
 * apps × 96×96 × 4 bytes ≈ 11 MB worst case.
 */
class AppsProvider(
    private val appContext: Context,
) {

    /** All installed applications, ready to render. */
    fun snapshot(): List<AppInfo> {
        val pm = appContext.packageManager
        val apps = runCatching {
            pm.getInstalledApplications(PackageManager.GET_META_DATA)
        }.getOrElse { return emptyList() }

        return apps.mapNotNull { info ->
            runCatching { buildAppInfo(pm, info) }.getOrNull()
        }.sortedBy { it.displayName.lowercase() }
    }

    /**
     * Icon-free rollup of every installed app. Used by `WifiProvider`
     * and `DataUsageProvider` where we only need packageName +
     * displayName (icons are loaded later per-row by the VM, lazily).
     *
     * Skips the expensive `getApplicationLabel` + `getApplicationInfo`
     * round-trip where the cached package list is enough.
     */
    fun simpleList(): List<AppInfo> {
        val pm = appContext.packageManager
        val apps = runCatching { pm.getInstalledApplications(0) }
            .getOrElse { return emptyList() }
        return apps.mapNotNull { info ->
            val pkg = info.packageName
            val label = runCatching { pm.getApplicationLabel(info).toString() }
                .getOrDefault(pkg)
            AppInfo(
                packageName = pkg,
                displayName = label,
                versionName = "—",
                categoryLabel = "",
                sizeBytes = 0L,
                lastUpdatedEpochMs = 0L,
                isSystem = (info.flags and ApplicationInfo.FLAG_SYSTEM) != 0,
                hasLauncher = false,
                iconRef = AppIconRef.Failed,
            )
        }
    }

    /** Cheap count rollup (no icons, no version lookups). */
    fun count(): InstalledAppsCount {
        val pm = appContext.packageManager
        val apps = runCatching {
            pm.getInstalledApplications(0)
        }.getOrElse { return InstalledAppsCount(0, 0, 0, 0L) }

        var userCount = 0
        var systemCount = 0
        var totalSize = 0L
        for (info in apps) {
            if ((info.flags and ApplicationInfo.FLAG_SYSTEM) != 0) {
                systemCount++
            } else {
                userCount++
            }
            // sourceDir is best-effort: skip nulls rather than fail.
            info.sourceDir?.let { path ->
                runCatching { totalSize += File(path).length() }
            }
        }
        return InstalledAppsCount(
            total = userCount + systemCount,
            user = userCount,
            system = systemCount,
            totalSizeBytes = totalSize,
        )
    }

    private fun buildAppInfo(pm: PackageManager, info: ApplicationInfo): AppInfo {
        val pkg = info.packageName
        val label = runCatching { pm.getApplicationLabel(info).toString() }
            .getOrDefault(pkg)
        val pkgInfo = runCatching { pm.getPackageInfo(pkg, 0) }.getOrNull()
        val version = pkgInfo?.versionName ?: "—"
        val lastUpdated = pkgInfo?.lastUpdateTime ?: 0L
        val isSystem = (info.flags and ApplicationInfo.FLAG_SYSTEM) != 0
        val hasLauncher = runCatching { pm.getLaunchIntentForPackage(pkg) != null }
            .getOrDefault(false)
        val categoryLabel = categoryLabel(info, pm)
        val sizeBytes = info.sourceDir?.let { path ->
            runCatching { File(path).length() }.getOrDefault(0L)
        } ?: 0L
        val iconRef = loadIconRef(pm, info)
        return AppInfo(
            packageName = pkg,
            displayName = label,
            versionName = version,
            categoryLabel = categoryLabel,
            sizeBytes = sizeBytes,
            lastUpdatedEpochMs = lastUpdated,
            isSystem = isSystem,
            hasLauncher = hasLauncher,
            iconRef = iconRef,
        )
    }

    private fun loadIconRef(pm: PackageManager, info: ApplicationInfo): AppIconRef {
        return runCatching {
            val drawable = pm.getApplicationIcon(info)
            val bitmap: Bitmap = drawable.toBitmap(
                width = ICON_PX,
                height = ICON_PX,
            )
            AppIconRef.Loaded(bitmap)
        }.getOrElse { AppIconRef.Failed }
    }

    private fun categoryLabel(info: ApplicationInfo, pm: PackageManager): String {
        // Direct mapping covers the categories defined by the
        // framework. Anything else falls back to "Apps" or "System"
        // based on the FLAG_SYSTEM bit.
        return when (info.category) {
            ApplicationInfo.CATEGORY_GAME -> "Games"
            ApplicationInfo.CATEGORY_AUDIO -> "Music & Audio"
            ApplicationInfo.CATEGORY_VIDEO -> "Video Players"
            ApplicationInfo.CATEGORY_IMAGE -> "Photography"
            ApplicationInfo.CATEGORY_SOCIAL -> "Social"
            ApplicationInfo.CATEGORY_NEWS -> "News"
            ApplicationInfo.CATEGORY_MAPS -> "Travel"
            ApplicationInfo.CATEGORY_PRODUCTIVITY -> "Productivity"
            else -> if ((info.flags and ApplicationInfo.FLAG_SYSTEM) != 0) "System" else "Apps"
        }
    }

    companion object {
        private const val ICON_PX = 96
    }
}
