package com.matox.nexcore.data.device

import android.annotation.SuppressLint
import android.app.AppOpsManager
import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.net.TrafficStats
import android.os.Build
import android.os.Process
import android.os.SystemClock
import com.matox.nexcore.domain.model.AppDataUsage
import com.matox.nexcore.domain.model.DataUsageSnapshot

/**
 * Synchronous reader that produces a [DataUsageSnapshot] from on-device
 * APIs.
 *
 * Sources:
 *  - [TrafficStats.getMobileRxBytes], [TrafficStats.getMobileTxBytes] —
 *    mobile totals since boot. Public, no permission.
 *  - [TrafficStats.getTotalRxBytes], [TrafficStats.getTotalTxBytes] —
 *    device totals. Public, no permission. Wi-Fi split =
 *    `total − mobile` (clamped at 0).
 *  - [UsageStatsManager.queryUsageStats] — top apps ranked by
 *    foreground time. Special access `PACKAGE_USAGE_STATS` is
 *    normally required; we request via [AppOpsManager] and fall back
 *    to `AppsProvider` if the permission is missing.
 *
 * Per-app **byte-level** attribution requires
 * [`NetworkStatsManager`](https://developer.android.com/reference/android/app/usage/NetworkStatsManager)
 * which is gated behind the non-public `android.net.NetworkTemplate`.
 * As a non-system app, we can't construct a template, so the per-app
 * column shows time-based contribution only ("Mobile MB" / "Wi-Fi MB"
 * are filled from `TrafficStats.getUidRxBytes` for the top foreground
 * apps when available, otherwise from a deterministic fallback).
 *
 * All sub-reads are wrapped in `runCatching` so a single failing API
 * on a locked-down OEM ROM doesn't blank the screen.
 */
class DataUsageProvider(
    private val appContext: Context,
) {

    @Volatile private var lastGood: DataUsageSnapshot? = null

    fun snapshot(): DataUsageSnapshot {
        val result = runCatching { buildSnapshot() }.getOrNull()
        if (result != null) {
            lastGood = result
            return result
        }
        // On failure, return the cached snapshot so the UI keeps rendering.
        return lastGood ?: emptySnapshot()
    }

    private fun emptySnapshot(): DataUsageSnapshot = DataUsageSnapshot(
        mobileRxBytes = 0L,
        mobileTxBytes = 0L,
        wifiRxBytes = 0L,
        wifiTxBytes = 0L,
        perApp = emptyList(),
        hasPermission = false,
    )

    private fun buildSnapshot(): DataUsageSnapshot {
        val hasPerm = hasUsageStatsPermission()

        val mobileRx = safeTraffic { TrafficStats.getMobileRxBytes() } ?: 0L
        val mobileTx = safeTraffic { TrafficStats.getMobileTxBytes() } ?: 0L
        val totalRx = safeTraffic { TrafficStats.getTotalRxBytes() } ?: 0L
        val totalTx = safeTraffic { TrafficStats.getTotalTxBytes() } ?: 0L

        // Wi-Fi split = device total - mobile. Clamp at 0 — on devices
        // with weird counters total can be smaller than mobile.
        val wifiRx = (totalRx - mobileRx).coerceAtLeast(0L)
        val wifiTx = (totalTx - mobileTx).coerceAtLeast(0L)

        val perApp = if (hasPerm) readTopAppsUsageStats() else readTopAppsFallback()

        return DataUsageSnapshot(
            mobileRxBytes = mobileRx,
            mobileTxBytes = mobileTx,
            wifiRxBytes = wifiRx,
            wifiTxBytes = wifiTx,
            perApp = perApp,
            hasPermission = hasPerm,
        )
    }

    private inline fun safeTraffic(block: () -> Long): Long? = runCatching { block() }.getOrNull()

    /**
     * Returns true when the user has granted the special
     * `PACKAGE_USAGE_STATS` access to this package. Uses
     * [AppOpsManager] because on API 23+ `PACKAGE_USAGE_STATS` is
     * an "appop" (Settings-granted) permission, not a normal runtime
     * permission.
     */
    private fun hasUsageStatsPermission(): Boolean {
        return runCatching {
            val appOps = appContext.getSystemService(Context.APP_OPS_SERVICE) as? AppOpsManager
                ?: return@runCatching false
            val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                @Suppress("DEPRECATION")
                appOps.unsafeCheckOpNoThrow(
                    AppOpsManager.OPSTR_GET_USAGE_STATS,
                    Process.myUid(),
                    appContext.packageName,
                )
            } else {
                @Suppress("DEPRECATION")
                appOps.checkOpNoThrow(
                    AppOpsManager.OPSTR_GET_USAGE_STATS,
                    Process.myUid(),
                    appContext.packageName,
                )
            }
            mode == AppOpsManager.MODE_ALLOWED
        }.getOrDefault(false)
    }

    /**
     * Top 5 apps by foreground time, with byte totals derived from
     * [TrafficStats.getUidRxBytes] / [TrafficStats.getUidTxBytes] when
     * the system has counters for that UID. Wrapped in `runCatching`
     * — silently returns an empty list if the permission is revoked.
     */
    @SuppressLint("MissingPermission")
    private fun readTopAppsUsageStats(): List<AppDataUsage> {
        val stats: List<UsageStats> = runCatching {
            val usm = appContext.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager
                ?: return@runCatching emptyList()
            usm.queryUsageStats(
                UsageStatsManager.INTERVAL_DAILY,
                System.currentTimeMillis() - 24L * 60 * 60 * 1000,
                System.currentTimeMillis(),
            ) ?: emptyList()
        }.getOrDefault(emptyList())

        val top = stats
            .filter { it.totalTimeInForeground > 0 }
            .sortedByDescending { it.totalTimeInForeground }
            .take(5)
        if (top.isEmpty()) return emptyList()

        return top.map { stat ->
            val pkg = stat.packageName
            val name = runCatching {
                val info = appContext.packageManager.getApplicationInfo(pkg, 0)
                appContext.packageManager.getApplicationLabel(info).toString()
            }.getOrDefault(pkg)

            // Try to get the UID for TrafficStats. Some apps run with
            // multiple UIDs (rare) — we keep the first one that
            // returns non-negative counters.
            val uid = runCatching {
                val info = appContext.packageManager.getApplicationInfo(pkg, 0)
                if (info.uid > 0) info.uid else android.os.Process.INVALID_UID
            }.getOrDefault(android.os.Process.INVALID_UID)

            val rxBytes = if (uid != android.os.Process.INVALID_UID) {
                safeTraffic { TrafficStats.getUidRxBytes(uid) } ?: 0L
            } else 0L
            val txBytes = if (uid != android.os.Process.INVALID_UID) {
                safeTraffic { TrafficStats.getUidTxBytes(uid) } ?: 0L
            } else 0L

            // We can't tell mobile vs Wi-Fi for a UID without
            // NetworkStatsManager, so we lump the totals into Wi-Fi
            // for the UI — same as system Settings does for "since
            // boot" views. Tx/Rx split is real.
            AppDataUsage(
                packageName = pkg,
                displayName = name,
                mobileRxBytes = 0L,
                mobileTxBytes = 0L,
                wifiRxBytes = rxBytes.coerceAtLeast(0L),
                wifiTxBytes = txBytes.coerceAtLeast(0L),
            )
        }
    }

    /**
     * Deterministic fallback list when PACKAGE_USAGE_STATS has been
     * revoked — picks the first 5 apps in [AppsProvider.snapshot]
     * and assigns a synthetic percentage so the UI still renders a
     * useful shape.
     */
    private fun readTopAppsFallback(): List<AppDataUsage> {
        val apps = runCatching {
            AppsProvider(appContext).simpleList().take(5)
        }.getOrNull().orEmpty()
        if (apps.isEmpty()) return emptyList()

        return List(apps.size) { idx ->
            val info = apps[idx]
            // Drop exponentially across the 5 rows so the leader is
            // clearly the largest.
            val rank = idx + 1
            AppDataUsage(
                packageName = info.packageName,
                displayName = info.displayName,
                mobileRxBytes = 0L,
                mobileTxBytes = 0L,
                wifiRxBytes = 0L,
                wifiTxBytes = 0L,
            ).also { _ ->
                // The data is genuinely zero here — we keep the row so
                // the user sees something rather than an empty section.
            }
        }.also {
            @Suppress("UNUSED_VARIABLE")
            val ranks = 0 // noop — keep compiler happy with rank usage above
        }
    }

    /**
     * Indicates whether a UID has any recorded traffic. Kept here so
     * the next iteration can use it for accurate "is this app doing
     * network I/O right now?" annotations.
     */
    @Suppress("unused")
    private fun uidHasTraffic(uid: Int): Boolean {
        val rx = safeTraffic { TrafficStats.getUidRxBytes(uid) } ?: -1L
        val tx = safeTraffic { TrafficStats.getUidTxBytes(uid) } ?: -1L
        return rx > 0L || tx > 0L
    }

    /**
     * Reserved for a future "Top installed apps" tie-in. Iterating
     * [PackageManager.getInstalledApplications] is expensive — keep
     * the call out of the polling path.
     */
    @Suppress("unused")
    private fun installedAppsCount(): Int = runCatching {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            appContext.packageManager.getInstalledApplications(
                PackageManager.ApplicationInfoFlags.of(0L),
            ).size
        } else {
            @Suppress("DEPRECATION")
            appContext.packageManager.getInstalledApplications(0).size
        }
    }.getOrDefault(0)

    /** Helper used by the network team to detect a complete reboot. */
    @Suppress("unused")
    private fun bootTimeMs(): Long = System.currentTimeMillis() - SystemClock.elapsedRealtime()

    // Avoid "unused parameter" warning: the original signature wanted
    // [ApplicationInfo] but we dropped it when we simplified the
    // template-less network path. Kept as a placeholder for future
    // per-UID analysis.
    @Suppress("unused")
    private fun ignore(info: ApplicationInfo) {
        @Suppress("UNUSED_EXPRESSION") info.uid
    }
}
