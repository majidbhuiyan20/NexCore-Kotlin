package com.matox.nexcore.presentation.dashboard

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.matox.nexcore.domain.model.BatteryDetails
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
import com.matox.nexcore.domain.model.StorageBreakdown
import com.matox.nexcore.domain.model.StorageCategory
import com.matox.nexcore.domain.model.StorageUsage
import com.matox.nexcore.domain.model.SystemMetric
import com.matox.nexcore.domain.model.UserGreeting
import com.matox.nexcore.presentation.dashboard.state.DashboardUiState
import com.matox.nexcore.ui.theme.NexCoreTheme

private val PreviewSnapshot = DashboardSnapshot(
    greeting = UserGreeting(
        userName = "Majid",
        greeting = "Good Morning",
        tagline = "Your device, Under Control.",
        detail = "Everything looks good",
        subtitle = "Keep it up!",
    ),
    nexCoreScore = NexCoreScore(
        value = 94,
        label = "NexCore Score",
        status = ScoreStatus.Excellent,
    ),
    metrics = listOf(
        SystemMetric(MetricType.RAM, "RAM", 62f, "4.1 GB", "/ 8 GB", MetricAccent.BLUE),
        SystemMetric(MetricType.STORAGE, "Storage", 48f, "123 GB", "/ 256 GB", MetricAccent.PURPLE),
        SystemMetric(MetricType.BATTERY, "Battery", 82f, "82%", "Charging", MetricAccent.GREEN),
        SystemMetric(MetricType.CPU, "CPU", 22f, "22%", "1.2 GHz", MetricAccent.ORANGE),
        SystemMetric(MetricType.TEMPERATURE, "Temp", 33f, "33°C", "Normal", MetricAccent.RED),
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
    storageUsage = StorageUsage(
        totalUsedGb = 123,
        breakdown = listOf(
            StorageBreakdown(StorageCategory.IMAGES, 45, MetricAccent.VIOLET),
            StorageBreakdown(StorageCategory.VIDEOS, 30, MetricAccent.BLUE),
            StorageBreakdown(StorageCategory.APPS, 18, MetricAccent.GREEN),
            StorageBreakdown(StorageCategory.DOCUMENTS, 8, MetricAccent.ORANGE),
            StorageBreakdown(StorageCategory.OTHERS, 22, MetricAccent.PINK),
        ),
    ),
    battery = BatteryDetails(percent = 82, isCharging = true, temperatureC = 33),
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

@Preview(showBackground = true, backgroundColor = 0xFF0B1220, widthDp = 412, heightDp = 1480)
@Composable
fun DashboardPreview() {
    NexCoreTheme {
        DashboardContent(
            state = DashboardUiState.Success(PreviewSnapshot),
            modifier = Modifier,
            onMenuClick = {},
            onSearchClick = {},
            onNotificationsClick = {},
            onInfoClick = {},
            onMetricClick = {},
            onHealthClick = {},
            onEditQuickActions = {},
            onQuickActionClick = {},
            onViewStorageDetails = {},
            onInfoCardClick = {},
            onBottomNavClick = {},
        )
    }
}