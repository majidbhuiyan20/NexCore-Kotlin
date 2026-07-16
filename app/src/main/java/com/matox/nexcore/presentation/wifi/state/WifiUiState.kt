package com.matox.nexcore.presentation.wifi.state

import android.graphics.Bitmap
import com.matox.nexcore.domain.model.WifiSnapshot

/**
 * UI state for the WiFi detail screen.
 *
 * - [Loading] — briefly visible until the first poll comes back.
 * - [Success] — every subsequent tick lands here. The snapshot
 *   carries its own rolling history so the hero ring and sub-labels
 *   can animate.
 * - [Error] — only triggered when the polling loop itself throws
 *   (the provider swallows individual sub-read failures, so this is
 *   unreachable under normal conditions).
 */
sealed interface WifiUiState {
    data object Loading : WifiUiState
    data class Success(
        val snapshot: WifiSnapshot,
        /** Map of `packageName → icon bitmap`, populated lazily by the
         *  ViewModel. Rows with no entry render a monogram fallback. */
        val appIcons: Map<String, Bitmap> = emptyMap(),
    ) : WifiUiState
    data class Error(val message: String) : WifiUiState
}
