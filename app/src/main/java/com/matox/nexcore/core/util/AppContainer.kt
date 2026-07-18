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
import kotlinx.coroutines.flow.Flow

/**
 * Tiny service locator. In a production project this would be replaced
 * by Hilt / Koin — kept dependency-free here for clarity.
 *
 * **Cold-start fast path.** `init(appContext)` only stores the
 * application context and pre-builds the lightweight providers (no
 * I/O). The expensive repositories are lazily built on first access
 * via [T::provide], so the dashboard doesn't pay for the WiFi
 * analyzer / Sensor Monitor / Data Usage providers until the user
 * actually opens those screens.
 *
 * **No `runBlocking`.** Earlier revisions called `runBlocking` here to
 * pre-emit the static dashboard base snapshot. That's bad on
 * `Application.onCreate` — the main thread stalls for tens of ms and
 * the splash screen hitches. We now build the static base snapshot
 * synchronously from in-memory data only (no I/O).
 */
object AppContainer {

    @Volatile private var appContext: Context? = null

    /** Providers (no I/O on construction) — built once on first access. */
    private val deviceProvider by lazy { DeviceMetricsProvider(requireContext()) }
    private val storageProvider by lazy { StorageAnalyzerProvider(requireContext()) }
    private val appsProvider by lazy { AppsProvider(requireContext()) }
    private val appsDataSource by lazy { LiveAppManagerDataSource(requireContext(), appsProvider) }
    private val phoneInfoProvider by lazy { PhoneInfoProvider(requireContext()) }
    private val ramProvider by lazy { RamProvider(requireContext()) }
    private val batteryProvider by lazy { BatteryProvider(requireContext()) }
    private val cpuProvider by lazy { CpuProvider(requireContext(), deviceProvider) }
    private val dataUsageProvider by lazy { DataUsageProvider(requireContext()) }
    private val wifiProvider by lazy { WifiProvider(requireContext()) }
    private val sensorProviderLocal by lazy { SensorProvider(requireContext()) }
    private val iconLoader by lazy { AppIconLoader(requireContext()) }

    /** Repositories — built lazily so each screen only pays for itself. */
    val dashboardRepository: DashboardRepository by lazy {
        DashboardRepositoryImpl(
            localDataSource = LiveDashboardLocalDataSource(
                base = FakeDashboardLocalDataSource().snapshotNow(),
                provider = deviceProvider,
                installedAppsCountFlow = appsDataSource.observeInstalledAppsCount(),
            ),
        )
    }
    val storageAnalyzerRepository: StorageAnalyzerRepository by lazy {
        StorageAnalyzerRepositoryImpl(
            dataSource = LiveStorageAnalyzerDataSource(storageProvider),
        )
    }
    val appManagerRepository: AppManagerRepository by lazy {
        AppManagerRepositoryImpl(appsDataSource)
    }
    val phoneInfoRepository: PhoneInfoRepository by lazy {
        PhoneInfoRepositoryImpl(phoneInfoProvider)
    }
    val ramRepository: RamRepository by lazy { RamRepositoryImpl(ramProvider) }
    val batteryRepository: BatteryRepository by lazy { BatteryRepositoryImpl(batteryProvider) }
    val cpuRepository: CpuRepository by lazy { CpuRepositoryImpl(cpuProvider) }
    val dataUsageRepository: DataUsageRepository by lazy {
        DataUsageRepositoryImpl(dataUsageProvider)
    }
    val wifiRepository: WifiRepository by lazy { WifiRepositoryImpl(wifiProvider) }
    val sensorRepository: SensorRepository by lazy {
        SensorRepositoryImpl(sensorProviderLocal)
    }

    /**
     * Backwards-compat accessor used by [SensorScreen] — exposes the
     * underlying provider so the ViewModel can register / unregister
     * its own `SensorEventListener`. Lazy-built alongside the sensor
     * repository.
     */
    val sensorProvider: SensorProvider get() = sensorProviderLocal

    val appIconLoader: AppIconLoader by lazy { iconLoader }

    /** Live installed-apps count shared with the home dashboard. */
    val installedAppsCountFlow: Flow<*> get() = appManagerRepository.observeInstalledAppsCount()

    /**
     * Called once from `NexCoreApplication.onCreate`. Stores the
     * application context and lets the lazy initialisers take over
     * from there. Deliberately does **no I/O** — anything that touches
     * PackageManager, BatteryManager, /proc, etc. is deferred to the
     * lazy getters.
     */
    fun init(appContext: Context) {
        this.appContext = appContext.applicationContext
    }

    private fun requireContext(): Context =
        appContext ?: error("AppContainer.init() not called yet")
}