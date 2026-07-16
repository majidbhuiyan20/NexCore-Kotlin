package com.matox.nexcore.domain.repository

import com.matox.nexcore.domain.model.BatterySnapshot
import kotlinx.coroutines.flow.Flow

/**
 * Repository contract for the Battery Monitor screen.
 *
 * [observeSnapshot] emits a fresh [BatterySnapshot] on each call. The
 * implementation polls the underlying providers so the hero card,
 * metric tiles, charts, and recommendations tick every 3 s.
 *
 * [refreshNow] requests an immediate poll outside the normal cadence
 * — the "Refresh" affordance (when present) and the "Battery Details"
 * sticky button can both call it.
 */
interface BatteryRepository {
    fun observeSnapshot(): Flow<BatterySnapshot>
    suspend fun refreshNow()
}
