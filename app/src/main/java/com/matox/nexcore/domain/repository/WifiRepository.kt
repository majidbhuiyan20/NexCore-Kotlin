package com.matox.nexcore.domain.repository

import com.matox.nexcore.domain.model.WifiSnapshot
import kotlinx.coroutines.flow.Flow

/**
 * Repository contract for the WiFi & Network screen.
 *
 * [observeSnapshot] emits a fresh [WifiSnapshot] on every poll.
 * The implementation polls every 3 s so the signal indicator and
 * link speed stay close to live without thrashing `WifiManager`.
 *
 * [refreshNow] requests an immediate poll + a fresh public-IP
 * lookup. [refreshPublicIp] fires a one-shot public-IP fetch and
 * publishes the result via the next emission.
 */
interface WifiRepository {
    fun observeSnapshot(): Flow<WifiSnapshot>
    suspend fun refreshNow()
    suspend fun refreshPublicIp()
}
