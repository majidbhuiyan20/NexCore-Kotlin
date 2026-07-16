package com.matox.nexcore.data.repository

import com.matox.nexcore.data.datasource.AppManagerDataSource
import com.matox.nexcore.domain.model.AppInfo
import com.matox.nexcore.domain.model.InstalledAppsCount
import com.matox.nexcore.domain.repository.AppManagerRepository
import kotlinx.coroutines.flow.Flow

class AppManagerRepositoryImpl(
    private val dataSource: AppManagerDataSource,
) : AppManagerRepository {

    override fun observeApps(): Flow<Pair<List<AppInfo>, Boolean>> = dataSource.observeApps()

    override fun observeInstalledAppsCount(): Flow<InstalledAppsCount> =
        dataSource.observeInstalledAppsCount()
}
