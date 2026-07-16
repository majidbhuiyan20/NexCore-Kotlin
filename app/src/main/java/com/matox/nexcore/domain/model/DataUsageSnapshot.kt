package com.matox.nexcore.domain.model

/**
 * Domain model for the Data Usage Monitor screen.
 *
 * - The "since boot" totals ([mobileRxBytes], [mobileTxBytes],
 *   [wifiRxBytes], [wifiTxBytes]) come from `TrafficStats` and work
 *   without any special permission.
 * - The per-app breakdown ([perApp]) is only populated when
 *   [hasPermission] is true — `PACKAGE_USAGE_STATS` (special access)
 *   is required for `NetworkStatsManager`.
 */
data class DataUsageSnapshot(
    val mobileRxBytes: Long,
    val mobileTxBytes: Long,
    val wifiRxBytes: Long,
    val wifiTxBytes: Long,
    val perApp: List<AppDataUsage>,
    /**
     * True when the user has granted the special
     * `PACKAGE_USAGE_STATS` access via Settings → Special access.
     * When false, the per-app section is hidden and the CTA card is
     * shown instead.
     */
    val hasPermission: Boolean,
) {
    val totalRxBytes: Long get() = mobileRxBytes + wifiRxBytes
    val totalTxBytes: Long get() = mobileTxBytes + wifiTxBytes
}

/** Per-package network usage since boot. Bytes are unsigned. */
data class AppDataUsage(
    val packageName: String,
    val displayName: String,
    val mobileRxBytes: Long,
    val mobileTxBytes: Long,
    val wifiRxBytes: Long,
    val wifiTxBytes: Long,
) {
    val totalBytes: Long
        get() = mobileRxBytes + mobileTxBytes + wifiRxBytes + wifiTxBytes
}
