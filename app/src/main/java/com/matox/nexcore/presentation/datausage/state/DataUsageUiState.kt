package com.matox.nexcore.presentation.datausage.state

import android.graphics.Bitmap
import com.matox.nexcore.domain.model.DataUsageSnapshot

/**
 * UI state for the Data Usage Monitor screen.
 *
 * - [Loading] — briefly visible until the first poll comes back.
 * - [Success] — every subsequent tick lands here.
 * - [Error] — only triggered when the polling loop itself throws.
 */
sealed interface DataUsageUiState {
    data object Loading : DataUsageUiState
    data class Success(
        val snapshot: DataUsageSnapshot,
        val appIcons: Map<String, Bitmap> = emptyMap(),
    ) : DataUsageUiState
    data class Error(val message: String) : DataUsageUiState
}
