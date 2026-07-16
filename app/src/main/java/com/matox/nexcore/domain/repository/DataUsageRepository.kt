package com.matox.nexcore.domain.repository

import com.matox.nexcore.domain.model.DataUsageSnapshot
import kotlinx.coroutines.flow.Flow

/**
 * Repository contract for the Data Usage Monitor screen.
 *
 * [observeSnapshot] emits a fresh [DataUsageSnapshot] every ~5 s. The
 * underlying provider pulls from `TrafficStats` (no permission) and
 * `NetworkStatsManager` (requires `PACKAGE_USAGE_STATS`).
 *
 * [refreshNow] requests an immediate poll outside the normal cadence.
 */
interface DataUsageRepository {
    fun observeSnapshot(): Flow<DataUsageSnapshot>
    suspend fun refreshNow()
}
