package com.matox.nexcore.domain.model

/**
 * Aggregated dashboard payload. This is what the UI consumes in one shot.
 *
 * The screen renders all sections from a single snapshot to keep the
 * ViewModel surface area small — each sub-section is just one field.
 */
data class DashboardSnapshot(
    val greeting: UserGreeting,
    val nexCoreScore: NexCoreScore,
    val metrics: List<SystemMetric>,
    val health: DeviceHealth,
    val quickActions: List<QuickAction>,
    val storageUsage: StorageUsage,
    val battery: BatteryDetails,
    val installedApps: InfoCardData,
    val dataUsage: InfoCardData,
    val notifications: InfoCardData,
    val bottomNav: List<BottomNavItem>,
)