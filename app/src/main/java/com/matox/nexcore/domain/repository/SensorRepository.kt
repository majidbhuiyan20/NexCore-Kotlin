package com.matox.nexcore.domain.repository

import com.matox.nexcore.domain.model.SensorSnapshot
import kotlinx.coroutines.flow.Flow

/**
 * Repository contract for the Sensor Monitor screen.
 *
 * [observeSnapshot] emits a fresh [SensorSnapshot] every time the
 * underlying [com.matox.nexcore.data.device.SensorProvider]
 * publishes one. The provider uses a SensorEventListener to react
 * to active sensor events, so emissions happen at the OS event
 * cadence (typically ~50 Hz for motion sensors) throttled down
 * to 10 Hz inside the provider to avoid recomposing Compose at
 * full sensor rate.
 *
 * [refreshNow] requests an immediate re-snapshot — useful after
 * sensor toggles (privacy settings) when the host wants to
 * re-render the list without waiting for the next event.
 *
 * [stopStreaming] tears down all registered listeners — only
 * useful when the host wants to drop battery consumption, the
 * UI otherwise keeps observers alive for its lifetime via the
 * provided Lifecycle.
 */
interface SensorRepository {
    fun observeSnapshot(): Flow<SensorSnapshot>
    suspend fun refreshNow()
    fun stopStreaming()
}
