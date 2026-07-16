package com.matox.nexcore.domain.repository

import com.matox.nexcore.domain.model.AppInfo
import com.matox.nexcore.domain.model.InstalledAppsCount
import kotlinx.coroutines.flow.Flow

/**
 * Repository contract for the App Manager screen.
 *
 * [observeApps] emits pairs of `(apps, fromCache)`. The first emission
 * after process start is almost always the disk-cached list (so the
 * screen paints in <50 ms); subsequent emissions are fresh
 * PackageManager snapshots with `fromCache = false`.
 */
interface AppManagerRepository {
    fun observeApps(): Flow<Pair<List<AppInfo>, Boolean>>
    fun observeInstalledAppsCount(): Flow<InstalledAppsCount>
}
