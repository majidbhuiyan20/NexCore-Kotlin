package com.matox.nexcore.presentation.ram.state

import android.graphics.Bitmap
import com.matox.nexcore.domain.model.RamSnapshot

/**
 * UI state for the RAM detail screen.
 *
 * - [Loading] — briefly visible until the first poll comes back.
 * - [Success] — every subsequent tick lands here. The snapshot
 *   carries its own rolling history so the chart can animate.
 * - [Error] — only triggered when the polling loop itself throws
 *   (the provider swallows individual sub-read failures, so this
 *   is unreachable under normal conditions).
 */
sealed interface RamUiState {
    data object Loading : RamUiState
    data class Success(
        val snapshot: RamSnapshot,
        /** Map of `packageName → icon bitmap`, populated lazily by the
         *  ViewModel. Rows with no entry in this map render a monogram
         *  fallback instead. */
        val appIcons: Map<String, Bitmap> = emptyMap(),
    ) : RamUiState
    data class Error(val message: String) : RamUiState
}