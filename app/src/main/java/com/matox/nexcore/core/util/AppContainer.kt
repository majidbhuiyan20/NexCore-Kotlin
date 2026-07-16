package com.matox.nexcore.core.util

import android.content.Context
import com.matox.nexcore.data.datasource.FakeDashboardLocalDataSource
import com.matox.nexcore.data.datasource.LiveDashboardLocalDataSource
import com.matox.nexcore.data.datasource.LiveStorageAnalyzerDataSource
import com.matox.nexcore.data.device.DeviceMetricsProvider
import com.matox.nexcore.data.device.StorageAnalyzerProvider
import com.matox.nexcore.data.repository.DashboardRepositoryImpl
import com.matox.nexcore.data.repository.StorageAnalyzerRepositoryImpl
import com.matox.nexcore.domain.repository.DashboardRepository
import com.matox.nexcore.domain.repository.StorageAnalyzerRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

/**
 * Tiny service locator. In a production project this would be replaced
 * by Hilt / Koin — kept dependency-free here for clarity.
 *
 * Initialised once from `NexCoreApplication.onCreate()` with the
 * application context. The dashboard repository is built eagerly
 * (cheap, in-memory) using the static base snapshot from
 * [FakeDashboardLocalDataSource]; the storage analyzer repo is
 * built on demand.
 */
object AppContainer {

    @Volatile private var initialized: Boolean = false

    lateinit var dashboardRepository: DashboardRepository
        private set
    lateinit var storageAnalyzerRepository: StorageAnalyzerRepository
        private set

    /**
     * Wire up repositories. Safe to call multiple times; subsequent
     * calls are no-ops.
     */
    fun init(appContext: Context) {
        if (initialized) return
        synchronized(this) {
            if (initialized) return
            val context = appContext.applicationContext

            // Build the static base snapshot once (off the main thread, but the
            // underlying Flow emission is synchronous so runBlocking is fine
            // for app startup).
            val baseSnapshot = runBlocking {
                FakeDashboardLocalDataSource().snapshot().first()
            }

            val deviceProvider = DeviceMetricsProvider(context)
            val local = LiveDashboardLocalDataSource(
                base = baseSnapshot,
                provider = deviceProvider,
            )
            dashboardRepository = DashboardRepositoryImpl(local)

            val storageProvider = StorageAnalyzerProvider(context)
            val storageDataSource = LiveStorageAnalyzerDataSource(storageProvider)
            storageAnalyzerRepository = StorageAnalyzerRepositoryImpl(storageDataSource)

            initialized = true
        }
    }
}
