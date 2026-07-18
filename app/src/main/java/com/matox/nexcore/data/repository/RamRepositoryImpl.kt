package com.matox.nexcore.data.repository

import com.matox.nexcore.data.device.RamProvider
import com.matox.nexcore.domain.model.RamSnapshot
import com.matox.nexcore.domain.repository.RamRepository
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
 * Polls [RamProvider.snapshot] every [POLL_INTERVAL_MS] and emits
 * the result. The provider's own rolling history buffer accumulates
 * samples between emissions, so successive emissions append one more
 * point to the chart on the detail screen.
 *
 * **Off-main-thread snapshot.** [RamProvider.snapshot] does heavy
 * I/O (PackageManager walks + ActivityManager.getProcessMemoryInfo
 * + /proc/meminfo reads). We wrap every snapshot in
 * `withContext(Dispatchers.IO)` and `flowOn(IO)` so the snapshot
 * runs on the IO pool and the main thread never blocks on these
 * reads.
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

    private val refreshRequests = Channel<Unit>(capacity = Channel.CONFLATED)

    override fun observeSnapshot(): Flow<RamSnapshot> = channelFlow {
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

    private suspend fun safeSnapshot(): RamSnapshot =
        withContext(Dispatchers.IO) { provider.snapshot() }

    companion object {
        /** 3 s cadence — RAM pressure changes meaningfully in seconds. */
        private const val POLL_INTERVAL_MS: Long = 3_000L
    }
}