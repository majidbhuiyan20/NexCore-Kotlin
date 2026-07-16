package com.matox.nexcore.domain.model

/**
 * Domain model for the CPU Monitor screen.
 *
 * Each [CpuSnapshot] carries both a "now" view (overall %, per-core
 * frequencies, etc.) and a rolling history buffer so the chart on the
 * detail screen can animate without re-querying the data layer.
 *
 * Pure data — no Compose / Android imports. The presentation layer
 * converts the [accent] hint through the theme palette.
 */
data class CpuSnapshot(
    /** 0..100 — current device-wide CPU utilisation. */
    val overallPercent: Int,

    /**
     * One entry per CPU core, in kHz / 1000 = MHz. A value of `0` means
     * the core is offline or its `scaling_cur_freq` was unreadable.
     * Capped at 8 cores for UI sanity even when the SoC has more.
     */
    val perCoreFrequenciesMhz: List<Int>,

    /** Total number of cores reported by the OS. */
    val coreCount: Int,

    /** Friendly SoC name — prefers `Build.SOC_MODEL` over `Build.HARDWARE`. */
    val socModel: String,

    /** Elapsed-realtime millis since boot (per [android.os.SystemClock]). */
    val uptimeMs: Long,

    /**
     * Rolling buffer of percent samples at ~1 Hz, oldest first.
     * 180 samples = last 3 minutes.
     */
    val historyPercent: List<Int>,

    /** Apps ranked by total CPU time in foreground over the last 24 h. */
    val topApps: List<CpuAppUsage>,
)

/** Per-app CPU contribution estimated from [android.app.usage.UsageStats]. */
data class CpuAppUsage(
    val packageName: String,
    val displayName: String,
    /** 0..100 — share of recent device CPU. */
    val estimatedPct: Float,
)
