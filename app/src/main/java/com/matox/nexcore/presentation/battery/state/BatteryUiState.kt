package com.matox.nexcore.presentation.battery.state

import android.graphics.Bitmap
import com.matox.nexcore.domain.model.BatterySnapshot

/**
 * UI state for the Battery Monitor screen.
 *
 * - [Loading] — briefly visible until the first poll comes back.
 * - [Success] — every subsequent tick lands here. The snapshot
 *   carries its own rolling history so the charts can animate.
 * - [Error] — only triggered when the polling loop itself throws
 *   (the provider swallows individual sub-read failures, so this is
 *   unreachable under normal conditions).
 */
sealed interface BatteryUiState {
    data object Loading : BatteryUiState
    data class Success(
        val snapshot: BatterySnapshot,
        /** Map of `packageName → icon bitmap`, populated lazily by the
         *  ViewModel. Rows with no entry render a monogram fallback. */
        val appIcons: Map<String, Bitmap> = emptyMap(),
    ) : BatteryUiState
    data class Error(val message: String) : BatteryUiState
}
