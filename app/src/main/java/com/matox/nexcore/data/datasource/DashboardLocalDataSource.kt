package com.matox.nexcore.data.datasource

import com.matox.nexcore.domain.model.BottomNavIcon
import com.matox.nexcore.domain.model.BottomNavItem
import com.matox.nexcore.domain.model.DashboardSnapshot
import com.matox.nexcore.domain.model.DeviceHealth
import com.matox.nexcore.domain.model.InfoCardData
import com.matox.nexcore.domain.model.MetricAccent
import com.matox.nexcore.domain.model.MetricType
import com.matox.nexcore.domain.model.NexCoreScore
import com.matox.nexcore.domain.model.QuickAction
import com.matox.nexcore.domain.model.QuickActionIcon
import com.matox.nexcore.domain.model.ScoreStatus
import com.matox.nexcore.domain.model.SystemMetric
import com.matox.nexcore.domain.model.UserGreeting
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Local data source that emits a static dashboard snapshot.
 * In a real app this would aggregate signals from system services,
 * battery manager, storage stats, etc.
 */
interface DashboardLocalDataSource {
    fun snapshot(): Flow<DashboardSnapshot>
}

class FakeDashboardLocalDataSource : DashboardLocalDataSource {

    override fun snapshot(): Flow<DashboardSnapshot> = flow {
        emit(buildSnapshot())
    }

    private fun buildSnapshot(): DashboardSnapshot = DashboardSnapshot(
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
        metrics = listOf(
            SystemMetric(
                id = MetricType.RAM,
                label = "RAM",
                valuePercent = 62f,
                primaryValue = "4.1 GB",
                secondaryValue = "/ 8 GB",
                accent = MetricAccent.BLUE,
            ),
            SystemMetric(
                id = MetricType.STORAGE,
                label = "Storage",
                valuePercent = 48f,
                primaryValue = "123 GB",
                secondaryValue = "/ 256 GB",
                accent = MetricAccent.PURPLE,
            ),
            SystemMetric(
                id = MetricType.BATTERY,
                label = "Battery",
                valuePercent = 82f,
                primaryValue = "82%",
                secondaryValue = "Charging",
                accent = MetricAccent.GREEN,
            ),
            SystemMetric(
                id = MetricType.CPU,
                label = "CPU",
                valuePercent = 22f,
                primaryValue = "22%",
                secondaryValue = "1.2 GHz",
                accent = MetricAccent.ORANGE,
            ),
            SystemMetric(
                id = MetricType.TEMPERATURE,
                label = "Temp",
                valuePercent = 33f,
                primaryValue = "33°C",
                secondaryValue = "Normal",
                accent = MetricAccent.RED,
            ),
        ),
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