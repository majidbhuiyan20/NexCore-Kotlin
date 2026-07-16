package com.matox.nexcore.presentation.storageanalyzer.state

import com.matox.nexcore.domain.model.StorageBreakdown

/**
 * UI state for the Storage Analyzer screen.
 */
sealed interface StorageAnalyzerUiState {
    data object Loading : StorageAnalyzerUiState
    data class Success(val snapshot: StorageBreakdown) : StorageAnalyzerUiState
    data class Error(val message: String) : StorageAnalyzerUiState
}
