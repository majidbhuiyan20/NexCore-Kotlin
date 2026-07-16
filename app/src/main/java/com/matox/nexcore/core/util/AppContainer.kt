package com.matox.nexcore.core.util

import android.content.Context
import com.matox.nexcore.data.datasource.FakeDashboardLocalDataSource
import com.matox.nexcore.data.datasource.LiveAppManagerDataSource
import com.matox.nexcore.data.datasource.LiveDashboardLocalDataSource
import com.matox.nexcore.data.datasource.LiveStorageAnalyzerDataSource
import com.matox.nexcore.data.device.AppIconLoader
import com.matox.nexcore.data.device.AppsProvider
import com.matox.nexcore.data.device.BatteryProvider
import com.matox.nexcore.data.device.CpuProvider
import com.matox.nexcore.data.device.DataUsageProvider
import com.matox.nexcore.data.device.DeviceMetricsProvider
import com.matox.nexcore.data.device.PhoneInfoProvider
import com.matox.nexcore.data.device.RamProvider
import com.matox.nexcore.data.device.SensorProvider
import com.matox.nexcore.data.device.StorageAnalyzerProvider
import com.matox.nexcore.data.device.WifiProvider
import com.matox.nexcore.data.repository.AppManagerRepositoryImpl
import com.matox.nexcore.data.repository.BatteryRepositoryImpl
import com.matox.nexcore.data.repository.CpuRepositoryImpl
import com.matox.nexcore.data.repository.DashboardRepositoryImpl
import com.matox.nexcore.data.repository.DataUsageRepositoryImpl
import com.matox.nexcore.data.repository.PhoneInfoRepositoryImpl
import com.matox.nexcore.data.repository.RamRepositoryImpl
import com.matox.nexcore.data.repository.SensorRepositoryImpl
import com.matox.nexcore.data.repository.StorageAnalyzerRepositoryImpl
import com.matox.nexcore.data.repository.WifiRepositoryImpl
import com.matox.nexcore.domain.repository.AppManagerRepository
import com.matox.nexcore.domain.repository.BatteryRepository
import com.matox.nexcore.domain.repository.CpuRepository
import com.matox.nexcore.domain.repository.DashboardRepository
import com.matox.nexcore.domain.repository.DataUsageRepository
import com.matox.nexcore.domain.repository.PhoneInfoRepository
import com.matox.nexcore.domain.repository.RamRepository
import com.matox.nexcore.domain.repository.SensorRepository
import com.matox.nexcore.domain.repository.StorageAnalyzerRepository
import com.matox.nexcore.domain.repository.WifiRepository
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
    lateinit var ramRepository: RamRepository
        private set
    lateinit var batteryRepository: BatteryRepository
        private set
    lateinit var cpuRepository: CpuRepository
        private set
    lateinit var dataUsageRepository: DataUsageRepository
        private set
    lateinit var wifiRepository: WifiRepository
        private set
    lateinit var sensorRepository: SensorRepository
        private set
    lateinit var sensorProvider: SensorProvider
        private set
    lateinit var appIconLoader: AppIconLoader
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
            val ramProvider = RamProvider(context)
            val batteryProvider = BatteryProvider(context)
            val cpuProvider = CpuProvider(context, deviceProvider)
            val dataUsageProvider = DataUsageProvider(context)
            val wifiProvider = WifiProvider(context)
            val sensorProvider = SensorProvider(context)
            val iconLoader = AppIconLoader(context)

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
            ramRepository = RamRepositoryImpl(ramProvider)
            batteryRepository = BatteryRepositoryImpl(batteryProvider)
            cpuRepository = CpuRepositoryImpl(cpuProvider)
            dataUsageRepository = DataUsageRepositoryImpl(dataUsageProvider)
            wifiRepository = WifiRepositoryImpl(wifiProvider)
            sensorRepository = SensorRepositoryImpl(sensorProvider)
            this.sensorProvider = sensorProvider
            appIconLoader = iconLoader

            initialized = true
        }
    }
}
