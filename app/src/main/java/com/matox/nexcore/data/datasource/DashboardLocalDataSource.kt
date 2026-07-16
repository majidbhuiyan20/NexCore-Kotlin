package com.matox.nexcore.data.datasource

import com.matox.nexcore.data.device.DeviceMetricsProvider
import com.matox.nexcore.domain.model.BottomNavIcon
import com.matox.nexcore.domain.model.BottomNavItem
import com.matox.nexcore.domain.model.DashboardSnapshot
import com.matox.nexcore.domain.model.DeviceMetrics
import com.matox.nexcore.domain.model.DeviceHealth
import com.matox.nexcore.domain.model.InfoCardData
import com.matox.nexcore.domain.model.MetricAccent
import com.matox.nexcore.domain.model.NexCoreScore
import com.matox.nexcore.domain.model.QuickAction
import com.matox.nexcore.domain.model.QuickActionIcon
import com.matox.nexcore.domain.model.ScoreStatus
import com.matox.nexcore.domain.model.UserGreeting
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Local data source that emits a dashboard snapshot.
 *
 * Two implementations:
 *  - [FakeDashboardLocalDataSource] — emits a single static
 *    snapshot; used by Compose previews and tests.
 *  - [LiveDashboardLocalDataSource] — emits the static base
 *    snapshot merged with a fresh [DeviceMetrics] every few seconds.
 */
interface DashboardLocalDataSource {
    fun snapshot(): Flow<DashboardSnapshot>
}

/**
 * Static-only data source. Snapshot is built from hardcoded values.
 * In a real app this would aggregate signals from system services,
 * battery manager, storage stats, etc.
 */
class FakeDashboardLocalDataSource : DashboardLocalDataSource {

    override fun snapshot(): Flow<DashboardSnapshot> = flow {
        emit(buildSnapshot(DeviceMetrics()))
    }

    private fun buildSnapshot(liveMetrics: DeviceMetrics): DashboardSnapshot = DashboardSnapshot(
        greeting = UserGreeting(
            userName = "Majid",
            greeting = "Good Morning",
            tagline = "Your device, Under Control.",
            detail = "Everything looks good",
            subtitle = "Keep it up!",
        ),
        nexCoreScore = NexCoreScore(
            value = 90,
            label = "NexCore Score",
            status = ScoreStatus.Excellent,
        ),
        liveMetrics = liveMetrics,
        health = DeviceHealth(
            title = "Your device is in great health",
            subtitle = "No issues found",
            isHealthy = true,
        ),
        quickActions = listOf(
            QuickAction("qa_storage", "Storage\nAnalyzer", QuickActionIcon.STORAGE_ANALYZER, MetricAccent.VIOLET),
            QuickAction("qa_cleaner", "Cleaner", QuickActionIcon.CLEANER, MetricAccent.GREEN),
            QuickAction("qa_files", "File\nManager", QuickActionIcon.FILE_MANAGER, MetricAccent.BLUE),
            QuickAction("qa_apps", "App\nManager", QuickActionIcon.APP_MANAGER, MetricAccent.BLUE),
            QuickAction("qa_battery", "Battery\nMonitor", QuickActionIcon.BATTERY_MONITOR, MetricAccent.GREEN),
            QuickAction("qa_network", "Network\nMonitor", QuickActionIcon.NETWORK_MONITOR, MetricAccent.PINK),
            QuickAction("qa_wifi", "WiFi\nAnalyzer", QuickActionIcon.WIFI_ANALYZER, MetricAccent.CYAN),
            QuickAction("qa_sensor", "Sensor\nMonitor", QuickActionIcon.SENSOR_MONITOR, MetricAccent.ORANGE),
            QuickAction("qa_phone", "Phone\nInfo", QuickActionIcon.PHONE_INFO, MetricAccent.CYAN),
            QuickAction("qa_backup", "Backup &\nRestore", QuickActionIcon.BACKUP_RESTORE, MetricAccent.PINK),
        ),
        installedApps = InfoCardData(
            id = "info_apps",
            title = "Installed Apps",
            bigValue = "302",
            unit = null,
            footnotePrimary = "User Apps: 157",
            footnoteSecondary = "System Apps: 145",
            accent = MetricAccent.PURPLE,
            showChevron = true,
        ),
        dataUsage = InfoCardData(
            id = "info_data",
            title = "Data Usage",
            bigValue = "2.45",
            unit = "GB",
            footnotePrimary = "Today",
            footnoteSecondary = null,
            accent = MetricAccent.CYAN,
            showChevron = false,
        ),
        notifications = InfoCardData(
            id = "info_notifications",
            title = "Notifications",
            bigValue = "5",
            unit = null,
            footnotePrimary = "Recent",
            footnoteSecondary = "No important alerts",
            accent = MetricAccent.PINK,
            showChevron = true,
        ),
        bottomNav = listOf(
            BottomNavItem("nav_home", "Home", BottomNavIcon.HOME, isCenter = false, isActive = true),
            BottomNavItem("nav_files", "Files", BottomNavIcon.FILES, isCenter = false, isActive = false),
            BottomNavItem("nav_apps", "Apps", BottomNavIcon.APPS, isCenter = true, isActive = false),
            BottomNavItem("nav_settings", "Settings", BottomNavIcon.SETTINGS, isCenter = false, isActive = false),
        ),
    )
}

/**
 * Live implementation. Emits the [base] snapshot merged with a
 * fresh [DeviceMetrics] snapshot every [pollIntervalMs] ms. The
 * base snapshot supplies the static UI bits (greeting, health,
 * quick actions, info cards, bottom nav); the live metrics
 * overlay is the only thing that changes between emissions.
 */
class LiveDashboardLocalDataSource(
    private val base: DashboardSnapshot,
    private val provider: DeviceMetricsProvider,
    private val pollIntervalMs: Long = 3_000L,
) : DashboardLocalDataSource {

    override fun snapshot(): Flow<DashboardSnapshot> = flow {
        while (true) {
            emit(base.copy(liveMetrics = provider.snapshot()))
            delay(pollIntervalMs)
        }
    }
}