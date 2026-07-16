package com.matox.nexcore.presentation.sensors.state

import com.matox.nexcore.domain.model.SensorLiveMotion
import com.matox.nexcore.domain.model.SensorSnapshot

/**
 * UI state for the Sensor Monitor screen.
 *
 * - [Loading] — visible until the first snapshot lands. The
 *   provider seeds an empty (zero-value) snapshot synchronously
 *   so this state is fleeting on real devices.
 * - [Success] — every subsequent sensor-event batch lands here.
 *   [liveMotion] carries the latest accel/gyro magnitudes used
 *   by the hero card's animated ring; the static sensor list
 *   reads from [snapshot].
 * - [Error] — only reached if the underlying flow throws, which
 *   is rare (the provider catches everything internally).
 */
sealed interface SensorUiState {
    data object Loading : SensorUiState

    data class Success(
        val snapshot: SensorSnapshot,
        val liveMotion: SensorLiveMotion = SensorLiveMotion.Empty,
    ) : SensorUiState

    data class Error(val message: String) : SensorUiState
}
