package com.matox.nexcore.data.repository

import com.matox.nexcore.data.device.RamProvider
import com.matox.nexcore.domain.model.RamSnapshot
import com.matox.nexcore.domain.repository.RamRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

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
 */
class RamRepositoryImpl(
    private val provider: RamProvider,
) : RamRepository {

    override fun observeSnapshot(): Flow<RamSnapshot> = flow {
        while (true) {
            runCatching { emit(provider.snapshot()) }
            delay(POLL_INTERVAL_MS)
        }
    }

    companion object {
        /** 3 s cadence — same as the dashboard metrics row. */
        private const val POLL_INTERVAL_MS: Long = 3_000L
    }
}