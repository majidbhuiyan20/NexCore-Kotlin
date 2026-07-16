package com.matox.nexcore.data.repository

import com.matox.nexcore.data.device.CpuProvider
import com.matox.nexcore.domain.model.CpuSnapshot
import com.matox.nexcore.domain.repository.CpuRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.merge

/**
 * Polls [CpuProvider.snapshot] every [POLL_INTERVAL_MS] and emits the
 * result.
 *
 * The CPU screen needs a tighter cadence than RAM/Battery (1 s vs. 3 s)
 * so the hero ring and per-core frequency bars feel "live". Each poll
 * is a single short file read so this stays cheap.
 *
 * Errors are caught silently and the loop continues — a single failed
 * read should not blank the chart. The provider itself caches the
 * last good snapshot, so callers keep getting data even on transient
 * failures.
 *
 * [refreshNow] unblocks the polling loop immediately so manual refresh
 * requests don't have to wait for the next tick.
 */
class CpuRepositoryImpl(
    private val provider: CpuProvider,
) : CpuRepository {

    private val refreshRequests = Channel<Unit>(capacity = Channel.CONFLATED)

    override fun observeSnapshot(): Flow<CpuSnapshot> = channelFlow {
        // First emit immediately so the UI doesn't have to wait 1 s.
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
        /** 1 s cadence — faster than RAM/Battery for a "live" feel. */
        private const val POLL_INTERVAL_MS: Long = 1_000L
    }
}
