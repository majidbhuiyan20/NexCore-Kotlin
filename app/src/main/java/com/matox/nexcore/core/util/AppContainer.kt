package com.matox.nexcore.core.util

import com.matox.nexcore.data.datasource.FakeDashboardLocalDataSource
import com.matox.nexcore.data.repository.DashboardRepositoryImpl
import com.matox.nexcore.domain.repository.DashboardRepository

/**
 * Tiny service locator. In a production project this would be replaced
 * by Hilt / Koin — kept dependency-free here for clarity.
 */
object AppContainer {
    val dashboardRepository: DashboardRepository by lazy {
        DashboardRepositoryImpl(localDataSource = FakeDashboardLocalDataSource())
    }
}
