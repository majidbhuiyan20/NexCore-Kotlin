package com.matox.nexcore.data.repository

import com.matox.nexcore.data.device.DataUsageProvider
import com.matox.nexcore.domain.model.DataUsageSnapshot
import com.matox.nexcore.domain.repository.DataUsageRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.merge

/**
 * Polls [DataUsageProvider.snapshot] every [POLL_INTERVAL_MS] and
 * emits the result.
 *
 * Data usage counters don't change perceptibly at 1 Hz — 5 s cadence
 * is plenty for the metrics tiles and the per-app ranking.
 *
 * Errors are caught silently and the loop continues — a single failed
 * read should not blank the chart. The provider itself caches the
 * last good snapshot, so callers keep getting data even on transient
 * failures.
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
        runCatching { send(provider.snapshot()) }

        val ticker = flow {
            while (true) {
                delay(POLL_INTERVAL_MS)
                emit(Unit)
            }
        }
        val refresher = refreshRequests.consumeAsFlow()

        merge(ticker, refresher).collect {
            runCatching { send(provider.snapshot()) }
        }
    }

    override suspend fun refreshNow() {
        refreshRequests.send(Unit)
    }

    companion object {
        /** 5 s cadence — data usage doesn't change fast. */
        private const val POLL_INTERVAL_MS: Long = 5_000L
    }
}
