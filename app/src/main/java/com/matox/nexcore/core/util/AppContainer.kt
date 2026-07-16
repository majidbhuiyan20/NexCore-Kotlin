package com.matox.nexcore.core.util

import android.content.Context
import com.matox.nexcore.data.datasource.FakeDashboardLocalDataSource
import com.matox.nexcore.data.datasource.LiveAppManagerDataSource
import com.matox.nexcore.data.datasource.LiveDashboardLocalDataSource
import com.matox.nexcore.data.datasource.LiveStorageAnalyzerDataSource
import com.matox.nexcore.data.device.AppsProvider
import com.matox.nexcore.data.device.DeviceMetricsProvider
import com.matox.nexcore.data.device.PhoneInfoProvider
import com.matox.nexcore.data.device.StorageAnalyzerProvider
import com.matox.nexcore.data.repository.AppManagerRepositoryImpl
import com.matox.nexcore.data.repository.DashboardRepositoryImpl
import com.matox.nexcore.data.repository.PhoneInfoRepositoryImpl
import com.matox.nexcore.data.repository.StorageAnalyzerRepositoryImpl
import com.matox.nexcore.domain.repository.AppManagerRepository
import com.matox.nexcore.domain.repository.DashboardRepository
import com.matox.nexcore.domain.repository.PhoneInfoRepository
import com.matox.nexcore.domain.repository.StorageAnalyzerRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

/**
 * Tiny service locator. In a production project this would be replaced
 * by Hilt / Koin — kept dependency-free here for clarity.
 *
 * Initialised once from `NexCoreApplication.onCreate()` with the
 * application context. All repositories are eagerly built because
 * none of them perform blocking work — flows expose the live data.
 */
object AppContainer {

    @Volatile private var initialized: Boolean = false

    lateinit var dashboardRepository: DashboardRepository
        private set
    lateinit var storageAnalyzerRepository: StorageAnalyzerRepository
        private set
    lateinit var appManagerRepository: AppManagerRepository
        private set
    lateinit var phoneInfoRepository: PhoneInfoRepository
        private set

    /** Live installed-apps count shared with the home dashboard. */
    val installedAppsCountFlow
        get() = if (initialized) (appManagerRepository.observeInstalledAppsCount())
        else throw IllegalStateException("AppContainer not initialised")

    fun init(appContext: Context) {
        if (initialized) return
        synchronized(this) {
            if (initialized) return
            val context = appContext.applicationContext

            // Build the static base snapshot once (the underlying Flow
            // emission is synchronous so runBlocking is fine here).
            val baseSnapshot = runBlocking {
                FakeDashboardLocalDataSource().snapshot().first()
            }

            val deviceProvider = DeviceMetricsProvider(context)
            val storageProvider = StorageAnalyzerProvider(context)
            val appsProvider = AppsProvider(context)
            val appsDataSource = LiveAppManagerDataSource(context, appsProvider)
            val phoneInfoProvider = PhoneInfoProvider(context)

            // The dashboard needs the live installed-apps count.
            dashboardRepository = DashboardRepositoryImpl(
                localDataSource = LiveDashboardLocalDataSource(
                    base = baseSnapshot,
                    provider = deviceProvider,
                    installedAppsCountFlow = appsDataSource.observeInstalledAppsCount(),
                ),
            )
            storageAnalyzerRepository = StorageAnalyzerRepositoryImpl(
                dataSource = LiveStorageAnalyzerDataSource(storageProvider),
            )
            appManagerRepository = AppManagerRepositoryImpl(appsDataSource)
            phoneInfoRepository = PhoneInfoRepositoryImpl(phoneInfoProvider)

            initialized = true
        }
    }
}
