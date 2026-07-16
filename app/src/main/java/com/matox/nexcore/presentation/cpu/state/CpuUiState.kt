package com.matox.nexcore.presentation.cpu.state

import android.graphics.Bitmap
import com.matox.nexcore.domain.model.CpuSnapshot

/**
 * UI state for the CPU Monitor screen.
 *
 * - [Loading] — briefly visible until the first poll comes back.
 * - [Success] — every subsequent tick lands here. The snapshot carries
 *   its own rolling history so the chart can animate.
 * - [Error] — only triggered when the polling loop itself throws
 *   (the provider swallows individual sub-read failures, so this is
 *   unreachable under normal conditions).
 */
sealed interface CpuUiState {
    data object Loading : CpuUiState
    data class Success(
        val snapshot: CpuSnapshot,
        /** Map of `packageName → icon bitmap`, populated lazily by the
         *  ViewModel. Rows with no entry render a monogram fallback. */
        val appIcons: Map<String, Bitmap> = emptyMap(),
    ) : CpuUiState
    data class Error(val message: String) : CpuUiState
}
