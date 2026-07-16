package com.matox.nexcore.data.repository

import com.matox.nexcore.data.device.RamProvider
import com.matox.nexcore.domain.model.RamSnapshot
import com.matox.nexcore.domain.repository.RamRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.launch

/**
 * Polls [RamProvider.snapshot] every [POLL_INTERVAL_MS] and emits the
 * result. The provider's own rolling history buffer accumulates
 * samples between emissions, so successive emissions append one more
 * point to the chart on the detail screen.
 *
 * Errors are caught silently and the loop continues — a single failed
 * read should not blank the chart. The provider itself caches the
 * last good snapshot, so callers keep getting data even on transient
 * failures.
 *
 * [refreshNow] unblocks the polling loop immediately so manual
 * refresh requests don't have to wait for the next tick.
 */
class RamRepositoryImpl(
    private val provider: RamProvider,
) : RamRepository {

    private val refreshRequests = kotlinx.coroutines.channels.Channel<Unit>(
        capacity = kotlinx.coroutines.channels.Channel.CONFLATED,
    )

    override fun observeSnapshot(): Flow<RamSnapshot> = channelFlow {
        // First emit immediately so the UI doesn't have to wait 3 s.
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
        /** 3 s cadence — same as the dashboard metrics row. */
        private const val POLL_INTERVAL_MS: Long = 3_000L
    }
}
