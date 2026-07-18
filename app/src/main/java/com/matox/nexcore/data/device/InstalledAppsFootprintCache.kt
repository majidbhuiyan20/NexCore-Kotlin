package com.matox.nexcore.data.device

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import java.io.File
import java.util.concurrent.atomic.AtomicReference

/**
 * Process-wide TTL cache for the on-disk footprint of every installed
 * app.
 *
 * `RamProvider` previously called [walkOnDiskFootprint] every 3 s.
 * On a typical phone with 200 apps, that's ~200 × `walkTopDown` file
 * tree scans per poll — several MB of stat() syscalls on the main
 * thread. We cache the result for [TTL_MS] so the dashboard can poll
 * RAM cheaply and only re-walk when the cache expires.
 *
 * The cache is **invalidated by `onNewIntent` / `onResume`** via
 * [invalidate], which the screens can call after returning from
 * installs/uninstalls. For background polling, the 30 s TTL is a
 * safe upper bound — apps don't change size meaningfully within that
 * window.
 *
 * Single-threaded access only — Android's UI thread is the only
 * caller, so we don't need locks; [AtomicReference] is enough to
 * guarantee visibility from any other coroutine that resumes on a
 * background dispatcher.
 */
object InstalledAppsFootprintCache {

    private data class Entry(
        val timestampMs: Long,
        val payload: Map<String, Long>,
        val labels: Map<String, String>,
        val isSystem: Map<String, Boolean>,
    )

    private val cached = AtomicReference<Entry?>(null)

    /**
     * Returns `(packageName → MB)` of on-disk footprint, refreshing
     * the cache if [TTL_MS] has elapsed since the last build.
     *
     * The full payload also includes a label + system-flag map (built
     * alongside the footprint walk) so callers don't have to do a
     * second PackageManager pass to render the top-apps list.
     */
    fun footprint(appContext: Context): Triple<Map<String, Long>, Map<String, String>, Map<String, Boolean>> {
        val now = System.currentTimeMillis()
        val current = cached.get()
        if (current != null && now - current.timestampMs < TTL_MS) {
            return Triple(current.payload, current.labels, current.isSystem)
        }
        val pm = appContext.packageManager
        val payload = HashMap<String, Long>()
        val labels = HashMap<String, String>()
        val isSystem = HashMap<String, Boolean>()
        val ourPkg = appContext.packageName
        runCatching {
            for (info in pm.getInstalledApplications(0)) {
                val pkg = info.packageName
                if (pkg == ourPkg) continue
                val label = runCatching { pm.getApplicationLabel(info).toString() }
                    .getOrDefault(pkg)
                labels[pkg] = label
                isSystem[pkg] = (info.flags and ApplicationInfo.FLAG_SYSTEM) != 0

                var bytes = 0L
                info.sourceDir?.let { p ->
                    runCatching { bytes += File(p).length() }
                }
                runCatching {
                    val dataDir = info.dataDir
                    if (!dataDir.isNullOrBlank()) {
                        val dir = File(dataDir)
                        if (dir.exists()) {
                            dir.walkTopDown().forEach { f ->
                                if (f.isFile) bytes += f.length()
                            }
                        }
                    }
                }
                if (bytes > 0L) payload[pkg] = bytes / (1024L * 1024L)
            }
        }
        cached.set(Entry(now, payload, labels, isSystem))
        return Triple(payload, labels, isSystem)
    }

    /** Drop the cache — call after returning from the App Manager /
     *  an install flow so the user sees fresh data immediately. */
    fun invalidate() {
        cached.set(null)
    }

    private const val TTL_MS: Long = 30_000L
}