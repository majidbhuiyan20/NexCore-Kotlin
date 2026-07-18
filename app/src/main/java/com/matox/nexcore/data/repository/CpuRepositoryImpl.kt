package com.matox.nexcore.data.repository

import com.matox.nexcore.data.device.CpuProvider
import com.matox.nexcore.domain.model.CpuSnapshot
import com.matox.nexcore.domain.repository.CpuRepository
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
 * Polls [CpuProvider.snapshot] every [POLL_INTERVAL_MS] and emits the
 * result.
 *
 * The CPU screen needs a tighter cadence than RAM/Battery (1 s vs. 3 s)
 * so the hero ring and per-core frequency bars feel "live". Each poll
 * is a single short file read so this stays cheap.
 *
 * **Off-main-thread.** CpuProvider reads `/proc/stat` and per-core
 * `scaling_cur_freq` files — fast, but we still dispatch onto
 * `Dispatchers.IO` so a slow read can never block the UI thread.
 *
 * [refreshNow] unblocks the polling loop immediately.
 */
class CpuRepositoryImpl(
    private val provider: CpuProvider,
) : CpuRepository {

    private val refreshRequests = Channel<Unit>(capacity = Channel.CONFLATED)

    override fun observeSnapshot(): Flow<CpuSnapshot> = channelFlow {
        // First emit immediately so the UI doesn't have to wait 1 s.
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

    private suspend fun safeSnapshot(): CpuSnapshot =
        withContext(Dispatchers.IO) { provider.snapshot() }

    companion object {
        /** 1 s cadence — faster than RAM/Battery for a "live" feel. */
        private const val POLL_INTERVAL_MS: Long = 1_000L
    }
}