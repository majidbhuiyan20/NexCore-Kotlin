package com.matox.nexcore.domain.repository

import com.matox.nexcore.domain.model.CpuSnapshot
import kotlinx.coroutines.flow.Flow

/**
 * Repository contract for the CPU Monitor screen.
 *
 * [observeSnapshot] emits a fresh [CpuSnapshot] on each poll. The CPU
 * implementation polls every 1 s (faster than RAM/Battery's 3 s) so
 * the hero ring and per-core frequency bars feel live.
 *
 * [refreshNow] requests an immediate poll outside the normal cadence
 * — the host can wire it to a manual refresh affordance.
 */
interface CpuRepository {
    fun observeSnapshot(): Flow<CpuSnapshot>
    suspend fun refreshNow()
}
