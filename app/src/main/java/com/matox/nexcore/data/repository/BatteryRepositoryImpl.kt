package com.matox.nexcore.data.repository

import com.matox.nexcore.data.device.BatteryProvider
import com.matox.nexcore.domain.model.BatterySnapshot
import com.matox.nexcore.domain.repository.BatteryRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.withContext

/**
 * Polls [BatteryProvider.snapshot] every [POLL_INTERVAL_MS] and emits
 * the result.
 *
 * **Off-main-thread snapshot.** `BatteryProvider.snapshot()` calls
 * `UsageStatsManager.queryUsageStats` and `PackageManager.getApplicationInfo`
 * for top-app enrichment. We `flowOn(Dispatchers.IO)` so the
 * snapshot runs on the IO pool.
 *
 * [refreshNow] unblocks the polling loop immediately.
 */
class BatteryRepositoryImpl(
    private val provider: BatteryProvider,
) : BatteryRepository {

    private val refreshRequests = Channel<Unit>(capacity = Channel.CONFLATED)

    override fun observeSnapshot(): Flow<BatterySnapshot> = channelFlow {
        // First emit immediately so the UI doesn't have to wait 3 s.
        runCatching { send(safeSnapshot()) }

        val ticker = flow {
            while (true) {
                delay(POLL_INTERVAL_MS)
                emit(Unit)
            }
        }
        val refresher = refreshRequests.consumeAsFlow()

        merge(ticker, refresher).collect {
            runCatching { send(safeSnapshot()) }
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun refreshNow() {
        refreshRequests.send(Unit)
    }

    private suspend fun safeSnapshot(): BatterySnapshot =
        withContext(Dispatchers.IO) { provider.snapshot() }

    companion object {
        /** 3 s cadence — battery levels change slowly. */
        private const val POLL_INTERVAL_MS: Long = 3_000L
    }
}