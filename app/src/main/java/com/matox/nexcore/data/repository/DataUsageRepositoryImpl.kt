package com.matox.nexcore.data.repository

import com.matox.nexcore.data.device.DataUsageProvider
import com.matox.nexcore.domain.model.DataUsageSnapshot
import com.matox.nexcore.domain.repository.DataUsageRepository
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
 * Polls [DataUsageProvider.snapshot] every [POLL_INTERVAL_MS] and
 * emits the result.
 *
 * Data usage counters don't change perceptibly at 1 Hz — 5 s cadence
 * is plenty for the metrics tiles and the per-app ranking.
 *
 * **Off-main-thread.** DataUsageProvider walks `TrafficStats.getUidRxBytes`
 * across hundreds of installed packages. We dispatch the snapshot to
 * `Dispatchers.IO` so the UI thread never blocks on that walk.
 *
 * [refreshNow] unblocks the polling loop immediately so manual refresh
 * requests don't have to wait for the next tick.
 */
class DataUsageRepositoryImpl(
    private val provider: DataUsageProvider,
) : DataUsageRepository {

    private val refreshRequests = Channel<Unit>(capacity = Channel.CONFLATED)

    override fun observeSnapshot(): Flow<DataUsageSnapshot> = channelFlow {
        // First emit immediately so the UI doesn't have to wait 5 s.
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

    private suspend fun safeSnapshot(): DataUsageSnapshot =
        withContext(Dispatchers.IO) { provider.snapshot() }

    companion object {
        /** 5 s cadence — data usage doesn't change fast. */
        private const val POLL_INTERVAL_MS: Long = 5_000L
    }
}