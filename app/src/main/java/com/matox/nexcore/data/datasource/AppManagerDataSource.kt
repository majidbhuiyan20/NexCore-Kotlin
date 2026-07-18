package com.matox.nexcore.data.datasource

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Handler
import android.os.Looper
import com.matox.nexcore.core.data.AppsCache
import com.matox.nexcore.data.device.AppsProvider
import com.matox.nexcore.domain.model.AppInfo
import com.matox.nexcore.domain.model.InstalledAppsCount
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

/**
 * Source for App Manager payloads.
 *
 * Two flows:
 *  - [observeApps] — first emits whatever the disk cache has (instant
 *    paint), then in the background reads PackageManager and emits a
 *    fresh snapshot, then keeps re-emitting whenever a package is
 *    installed, removed, or replaced. Disk writes go through [AppsCache]
 *    so the next open is also instant.
 *  - [observeInstalledAppsCount] — same broadcast-driven pattern but
 *    for the cheap `count()` rollup used by the home dashboard.
 *
 * The cache + broadcast pairing means App Manager never blocks on
 * PackageManager: the screen paints from disk in <50 ms and then
 * swaps to a fresh snapshot once PackageManager returns (~300 ms
 * on a device with ~200 apps).
 */
interface AppManagerDataSource {
    /**
     * Emits `(apps, fromCache)` pairs. The first emission is almost
     * always from the disk cache (`fromCache = true`); subsequent
     * emissions are fresh PackageManager snapshots (`fromCache = false`).
     */
    fun observeApps(): Flow<Pair<List<AppInfo>, Boolean>>
    fun observeInstalledAppsCount(): Flow<InstalledAppsCount>
}

class LiveAppManagerDataSource(
    private val appContext: Context,
    private val provider: AppsProvider,
) : AppManagerDataSource {

    private val cache = AppsCache(appContext)

    /**
     * Cached apps snapshot. Held in memory so the disk cache only ever
     * gets read on the first emission after process start.
     */
    @Volatile
    private var cachedApps: List<AppInfo>? = null

    /**
     * Hot flow that re-emits whenever the package set changes. The
     * initial value is `Unit` so subscribers don't sit idle waiting
     * for the first broadcast.
     */
    private val packageEvents = MutableSharedFlow<Unit>(
        replay = 0,
        extraBufferCapacity = 8,
    )

    override fun observeApps(): Flow<Pair<List<AppInfo>, Boolean>> = flow {
        // 1. Cached snapshot (if any) — paints the screen instantly.
        val initial = cachedApps ?: cache.read()?.also { cachedApps = it }
        if (initial != null) emit(initial to true)

        // 2. Always refresh in the background so the next emission is
        //    accurate. We don't `await` it — the cached one above is
        //    already on screen.
        val fresh = provider.snapshot()
        cachedApps = fresh
        cache.write(fresh)
        emit(fresh to false)

        // 3. Re-snapshot whenever any package changes.
        packageEvents.collect {
            val next = provider.snapshot()
            cachedApps = next
            cache.write(next)
            emit(next to false)
        }
    }
        .flowOn(Dispatchers.IO)
        .onStart { ensurePackageReceiver() }

    override fun observeInstalledAppsCount(): Flow<InstalledAppsCount> = callbackFlow {
        // Initial count runs on IO so the first emission doesn't block
        // the main thread while PackageManager enumerates installed apps.
        launch(Dispatchers.IO) { trySend(provider.count()) }.also { it.join() }

        val handler = Handler(Looper.getMainLooper())
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                handler.removeCallbacksAndMessages(null)
                handler.postDelayed({
                    launch(Dispatchers.IO) {
                        trySend(provider.count())
                        packageEvents.tryEmit(Unit)
                    }
                }, 250L)
            }
        }
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_PACKAGE_ADDED)
            addAction(Intent.ACTION_PACKAGE_REMOVED)
            addAction(Intent.ACTION_PACKAGE_REPLACED)
            addAction(Intent.ACTION_PACKAGE_CHANGED)
            addDataScheme("package")
        }
        appContext.registerReceiver(receiver, filter)

        var tickerJob: Job? = null
        tickerJob = launch(Dispatchers.IO) {
            while (true) {
                delay(60_000L)
                trySend(provider.count())
            }
        }

        awaitClose {
            handler.removeCallbacksAndMessages(null)
            runCatching { appContext.unregisterReceiver(receiver) }
            tickerJob?.cancel()
        }
    }

    private var receiverStarted = false
    private val receiverLock = Any()

    private fun ensurePackageReceiver() {
        if (receiverStarted) return
        synchronized(receiverLock) {
            if (receiverStarted) return
            receiverStarted = true
            val handler = Handler(Looper.getMainLooper())
            val receiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context, intent: Intent) {
                    handler.removeCallbacksAndMessages(null)
                    handler.postDelayed({
                        packageEvents.tryEmit(Unit)
                    }, 250L)
                }
            }
            val filter = IntentFilter().apply {
                addAction(Intent.ACTION_PACKAGE_ADDED)
                addAction(Intent.ACTION_PACKAGE_REMOVED)
                addAction(Intent.ACTION_PACKAGE_REPLACED)
                addAction(Intent.ACTION_PACKAGE_CHANGED)
                addDataScheme("package")
            }
            appContext.registerReceiver(receiver, filter)
        }
    }
}
