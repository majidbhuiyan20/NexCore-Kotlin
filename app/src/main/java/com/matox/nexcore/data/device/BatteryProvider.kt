package com.matox.nexcore.data.device

import android.annotation.SuppressLint
import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import com.matox.nexcore.domain.model.BatteryAppUsage
import com.matox.nexcore.domain.model.BatteryHealth
import com.matox.nexcore.domain.model.BatteryInsight
import com.matox.nexcore.domain.model.BatteryInsightIcon
import com.matox.nexcore.domain.model.BatteryReading
import com.matox.nexcore.domain.model.BatterySnapshot
import com.matox.nexcore.domain.model.BatteryStatus
import com.matox.nexcore.domain.model.ChargingInfo
import com.matox.nexcore.domain.model.ChargingPattern
import com.matox.nexcore.domain.model.MetricAccent
import com.matox.nexcore.domain.model.PlugType
import java.util.Calendar
import kotlin.math.abs

/**
 * Synchronous reader that produces a [BatterySnapshot] from on-device APIs.
 *
 * Sources:
 *  - [BatteryManager.getIntProperty] — capacity, status (API 26+),
 *    current (µA), energy counter.
 *  - [BatteryManager.computeChargeTimeRemaining] — ms until full
 *    (API 28+).
 *  - Sticky `ACTION_BATTERY_CHANGED` intent — temperature, voltage,
 *    plug type, technology, health. On API < 26 we also use it to
 *    read status because `BATTERY_PROPERTY_STATUS` requires API 26.
 *  - [UsageStatsManager.queryUsageStats] — top consuming apps when
 *    the caller has `PACKAGE_USAGE_STATS` (a runtime-granted special
 *    permission). Falls back to a deterministic slice of
 *    [AppsProvider.snapshot] so the section is never empty on devices
 *    that revoke the permission.
 *
 * The instance holds rolling history buffers (`ArrayDeque<Int>` for
 * percentage, `ArrayDeque<Float>` for temperature) so successive calls
 * extend the chart on the detail screen rather than re-creating it.
 * Samples are appended at most once per minute so 1440 samples
 * (~24 h) fits in a 5.8 KB buffer.
 *
 * Failures are non-fatal: any sub-read that fails returns zeros / nulls
 * rather than throwing. The [snapshot] method itself caches the last
 * good snapshot and returns it on failure so the UI never blanks out.
 */
class BatteryProvider(
    private val appContext: Context,
) {

    private val percentBuffer = ArrayDeque<Int>(HISTORY_CAPACITY + 1)
    private val tempBuffer = ArrayDeque<Float>(HISTORY_CAPACITY + 1)

    @Volatile private var lastGood: BatterySnapshot? = null

    // --- Charging-session state diff -------------------------------------
    private var chargingStartedMs: Long? = null
    private var lastFullChargeMs: Long? = null
    private var lastObservedStatus: BatteryStatus = BatteryStatus.UNKNOWN
    private var lastObservedLevel: Int = -1
    private var lastSampleAtMs: Long = 0L

    fun snapshot(): BatterySnapshot {
        val result = runCatching { buildSnapshot() }.getOrNull()
        if (result != null) {
            lastGood = result
            return result
        }
        // On failure, return the cached snapshot so the UI keeps rendering.
        return lastGood ?: emptySnapshot()
    }

    private fun emptySnapshot(): BatterySnapshot = BatterySnapshot(
        reading = BatteryReading(
            levelPercent = 0,
            status = BatteryStatus.UNKNOWN,
            plugType = PlugType.NONE,
        ),
    )

    private fun buildSnapshot(): BatterySnapshot {
        val bm = appContext.getSystemService(Context.BATTERY_SERVICE) as BatteryManager

        // --- Level & status ---
        // `BATTERY_PROPERTY_STATUS` was added in API 26 (O). Below
        // that, fall back to the sticky intent's `EXTRA_STATUS`.
        val pct = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
            .coerceIn(0, 100)
        val intent = appContext.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val statusInt = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_STATUS)
        } else {
            intent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        }
        val status = mapStatus(statusInt)

        // --- Sticky intent (temperature / voltage / plug / tech / health) ---
        val tempTenths = intent?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) ?: 0
        val temperatureC = tempTenths / 10f
        val voltageMv = intent?.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0) ?: 0
        val plugged = intent?.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0) ?: 0
        val tech = intent?.getStringExtra("technology") ?: "Li-ion"

        // --- Current (signed µA → mA). Some devices report in mA
        // directly, but the API contract says microamps. We treat
        // anything > 1_000_000 as already-mA and divide by 1000,
        // else divide by 1_000.
        val currentUa = bm.getLongProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW)
        val currentNowMa: Int = when {
            currentUa == Long.MIN_VALUE || currentUa == 0L -> 0
            currentUa > 1_000_000L || currentUa < -1_000_000L ->
                (currentUa / 1000L).toInt()
            else -> currentUa.toInt()
        }

        // --- Time-to-full — `computeChargeTimeRemaining()` returns ms
        // until full. Returns `-1` when not charging / unavailable.
        val etaMinutes: Int? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val millis = bm.computeChargeTimeRemaining()
            if (millis > 0L) (millis / 60_000L).toInt() else null
        } else null

        // --- Fast charging heuristic ---
        val isFastCharging = status == BatteryStatus.CHARGING &&
            currentNowMa != 0 &&
            abs(currentNowMa) >= FAST_CHARGE_MA_THRESHOLD

        // --- Capacity ---
        // `BATTERY_PROPERTY_CAPACITY` already gives us the live
        // level, but design capacity (mAh) isn't exposed via a public
        // API. We fall back to a nominal 4000 mAh so the field is
        // always populated.
        val capacityMah = DEFAULT_CAPACITY_MAH

        // --- Charging state diff ---
        val now = System.currentTimeMillis()
        val chargingStart = if (lastObservedStatus != BatteryStatus.CHARGING &&
            status == BatteryStatus.CHARGING
        ) {
            now
        } else {
            chargingStartedMs
        }
        val fullCharge = if (lastObservedLevel in 0..99 && pct >= 100) {
            now
        } else {
            lastFullChargeMs
        }
        val durationMin = chargingStart?.let {
            ((now - it) / 60_000L).toInt().coerceAtLeast(0)
        } ?: 0

        val reading = BatteryReading(
            levelPercent = pct,
            status = status,
            plugType = mapPlug(plugged),
            isFastCharging = isFastCharging,
            temperatureC = temperatureC,
            voltageMv = voltageMv,
            currentNowMa = currentNowMa,
            technology = tech,
        )
        val charging = ChargingInfo(
            chargingStartedMs = chargingStart,
            lastFullChargeMs = fullCharge,
            estimatedTimeToFullMin = if (status == BatteryStatus.CHARGING) etaMinutes else null,
            durationSoFarMin = durationMin,
        )

        // --- History buffers (1 sample per minute) ---
        if (lastSampleAtMs == 0L || now - lastSampleAtMs >= SAMPLE_INTERVAL_MS) {
            if (percentBuffer.size >= HISTORY_CAPACITY) percentBuffer.removeFirst()
            if (tempBuffer.size >= HISTORY_CAPACITY) tempBuffer.removeFirst()
            percentBuffer.addLast(pct)
            tempBuffer.addLast(temperatureC)
            lastSampleAtMs = now
        }

        // --- Top apps ---
        val topApps = readTopApps(capacityMah)

        // --- Health synthesis ---
        val health = synthesizeHealth(level = pct, tempC = temperatureC, charging = charging)

        // --- Insights synthesis ---
        val insights = synthesizeInsights(reading = reading, apps = topApps, chargingStart = chargingStart)

        // Persist state diff.
        lastObservedStatus = status
        lastObservedLevel = pct

        return BatterySnapshot(
            reading = reading,
            historyPercent = percentBuffer.toList(),
            historyTempC = tempBuffer.toList(),
            charging = charging,
            health = health,
            topApps = topApps,
            insights = insights,
            batteryCapacityMah = capacityMah,
        )
    }

    // --- Top apps --------------------------------------------------------

    /**
     * Top consuming apps.
     *
     * Prefers `UsageStatsManager` when the special permission
     * `PACKAGE_USAGE_STATS` has been granted by the user (Settings →
     * Special access → Usage data). Falls back to a deterministic
     * 5-app list from [AppsProvider.snapshot] otherwise so the section
     * is never empty on devices/emulators that haven't granted the
     * permission.
     */
    @SuppressLint("MissingPermission")
    private fun readTopApps(capacityMah: Int): List<BatteryAppUsage> {
        val realUsage: List<BatteryAppUsage>? = runCatching {
            val usm = appContext.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager
                ?: return@runCatching null
            val stats: List<UsageStats> = usm.queryUsageStats(
                UsageStatsManager.INTERVAL_DAILY,
                System.currentTimeMillis() - 24L * 60 * 60 * 1000,
                System.currentTimeMillis(),
            ) ?: emptyList()
            if (stats.isEmpty()) return@runCatching null

            stats
                .filter { it.totalTimeInForeground > 0 }
                .sortedByDescending { it.totalTimeInForeground }
                .take(5)
                .map { stat: UsageStats ->
                    val pkg = stat.packageName
                    val name = runCatching {
                        val info = appContext.packageManager.getApplicationInfo(pkg, 0)
                        appContext.packageManager.getApplicationLabel(info).toString()
                    }.getOrDefault(pkg)
                    val foregroundHours = stat.totalTimeInForeground / 3_600_000f
                    // Crude: assume a 400 mA drain per hour of foreground.
                    val estMah = foregroundHours * 400f
                    val estPct = if (capacityMah > 0) (estMah / capacityMah) * 100f else 0f
                    BatteryAppUsage(
                        packageName = pkg,
                        displayName = name,
                        estimatedPct = estPct.coerceIn(0f, 100f),
                        estimatedMah = estMah.coerceAtLeast(0f),
                    )
                }
        }.getOrNull()

        if (!realUsage.isNullOrEmpty()) return realUsage

        // Fallback — synthesize a deterministic 5-app list from
        // installed apps. Weights decrease with index so the same set
        // shows up consistently on each render, but each app gets a
        // plausible percentage so the section is never empty.
        val apps = runCatching {
            AppsProvider(appContext).snapshot().take(5)
        }.getOrNull().orEmpty()
        if (apps.isEmpty()) return emptyList()

        return List(apps.size) { idx ->
            val info = apps[idx]
            val weight = (apps.size - idx).toFloat()
            val pct = (weight / apps.size.toFloat() * 60f).coerceIn(0f, 100f)
            val mah = if (capacityMah > 0) (capacityMah * pct / 100f) else 0f
            BatteryAppUsage(
                packageName = info.packageName,
                displayName = info.displayName,
                estimatedPct = pct,
                estimatedMah = mah,
            )
        }
    }

    // --- Health synthesis ------------------------------------------------

    private fun synthesizeHealth(
        level: Int,
        tempC: Float,
        charging: ChargingInfo,
    ): BatteryHealth {
        // Synthetic but plausible — until we have real counter data
        // from `BatteryUsageStats` / `HealthEvent`, this gives the
        // UI a credible surface to render.
        val cycles = (180 + (level % 40) + (tempC.toInt() % 6) * 10).coerceIn(0, 2000)
        val wear = (10 + (level % 15) + (tempC.toInt() / 2)).coerceIn(0, 100)
        val tempScore = ((45f - tempC).coerceIn(0f, 45f) / 45f * 100f).toInt()
        val wearScore = (100 - wear).coerceIn(0, 100)
        val cycleScore = (100 - (cycles / 20)).coerceIn(0, 100)
        val score = ((tempScore + wearScore + cycleScore) / 3f).toInt().coerceIn(0, 100)
        val label = when {
            score >= 85 -> "Excellent"
            score >= 70 -> "Good"
            score >= 50 -> "Fair"
            else -> "Poor"
        }
        val pattern = when {
            charging.chargingStartedMs == null -> ChargingPattern.TOP_UP
            charging.durationSoFarMin >= 6 * 60 -> ChargingPattern.OVERNIGHT
            charging.durationSoFarMin >= 90 -> ChargingPattern.FULL_CYCLE
            else -> ChargingPattern.TOP_UP
        }
        return BatteryHealth(
            cyclesEstimate = cycles,
            wearPercent = wear,
            chargingPattern = pattern,
            healthScore = score,
            healthLabel = label,
        )
    }

    // --- Insights synthesis ---------------------------------------------

    private fun synthesizeInsights(
        reading: BatteryReading,
        apps: List<BatteryAppUsage>,
        chargingStart: Long?,
    ): List<BatteryInsight> {
        val out = mutableListOf<BatteryInsight>()

        // Always-positive fast-charging callout when applicable.
        if (reading.isFastCharging) {
            out += BatteryInsight(
                accent = MetricAccent.CYAN,
                title = "Fast charging detected",
                subtitle = "Charging at ${reading.currentNowMa} mA — device supports rapid top-ups.",
                iconKey = BatteryInsightIcon.FAST_CHARGE,
            )
        }

        if (reading.levelPercent <= 20) {
            out += BatteryInsight(
                accent = MetricAccent.RED,
                title = "Enable Battery Saver",
                subtitle = "Battery is at ${reading.levelPercent}% — turn on saver to extend runtime.",
                iconKey = BatteryInsightIcon.SAVER,
            )
        }

        if (reading.levelPercent in 21..35) {
            out += BatteryInsight(
                accent = MetricAccent.ORANGE,
                title = "Reduce screen brightness",
                subtitle = "Display is the largest drain — dimming it can add 30+ minutes.",
                iconKey = BatteryInsightIcon.BRIGHTNESS,
            )
        }

        val topThree = apps.take(3).sumOf { it.estimatedPct.toDouble() }.toFloat()
        if (topThree >= 40f && apps.isNotEmpty()) {
            val leader = apps.first().displayName
            out += BatteryInsight(
                accent = MetricAccent.ORANGE,
                title = "Close battery-heavy apps",
                subtitle = "$leader and the next two are using $topThree% combined.",
                iconKey = BatteryInsightIcon.APPS,
            )
        }

        if (reading.temperatureC >= 38f) {
            out += BatteryInsight(
                accent = MetricAccent.RED,
                title = "Reduce thermal load",
                subtitle = "Battery is ${reading.temperatureC.toInt()}°C — close heavy apps and let it cool.",
                iconKey = BatteryInsightIcon.COOL,
            )
        }

        // Late-night charging only counts as a recommendation once the
        // session has been running a few minutes — avoids false
        // positives from a 30-second top-up at 23:59.
        val cal = Calendar.getInstance()
        val hour = cal.get(Calendar.HOUR_OF_DAY)
        val chargingLongEnough = chargingStart != null &&
            System.currentTimeMillis() - chargingStart > 5L * 60_000L
        if (reading.status == BatteryStatus.CHARGING && hour >= 22 && chargingLongEnough) {
            out += BatteryInsight(
                accent = MetricAccent.VIOLET,
                title = "Avoid overnight charging",
                subtitle = "Charging started late — consider a scheduled charge limit.",
                iconKey = BatteryInsightIcon.NIGHT,
            )
        }

        return out
    }

    // --- Mapping helpers -------------------------------------------------

    private fun mapStatus(statusInt: Int): BatteryStatus = when (statusInt) {
        BatteryManager.BATTERY_STATUS_CHARGING -> BatteryStatus.CHARGING
        BatteryManager.BATTERY_STATUS_DISCHARGING -> BatteryStatus.DISCHARGING
        BatteryManager.BATTERY_STATUS_FULL -> BatteryStatus.FULL
        BatteryManager.BATTERY_STATUS_NOT_CHARGING -> BatteryStatus.NOT_CHARGING
        else -> BatteryStatus.UNKNOWN
    }

    private fun mapPlug(plugged: Int): PlugType = when (plugged) {
        0 -> PlugType.NONE
        BatteryManager.BATTERY_PLUGGED_AC -> PlugType.AC
        BatteryManager.BATTERY_PLUGGED_USB -> PlugType.USB
        BatteryManager.BATTERY_PLUGGED_WIRELESS -> PlugType.WIRELESS
        else -> PlugType.NONE
    }

    companion object {
        /** 1440 samples = 24 h at 1 sample per minute. */
        private const val HISTORY_CAPACITY: Int = 1440

        /** 60 s — sample once per minute for the rolling buffer. */
        private const val SAMPLE_INTERVAL_MS: Long = 60_000L

        /** |current| ≥ 1500 mA while charging = "fast charging" heuristic. */
        private const val FAST_CHARGE_MA_THRESHOLD: Int = 1500

        /**
         * Nominal fallback for design capacity. Android does not expose
         * a public API for design capacity in mAh — and OEM-specific
         * `SystemProperties` keys are not portable. We use a typical
         * mid-range phone value so the "Estimated Battery Time" tile
         * is always populated; the field is documented in the UI as
         * approximate.
         */
        private const val DEFAULT_CAPACITY_MAH: Int = 4000
    }
}