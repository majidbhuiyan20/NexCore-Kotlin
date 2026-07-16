package com.matox.nexcore.presentation.files.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.matox.nexcore.domain.repository.StorageAnalyzerRepository
import com.matox.nexcore.presentation.storageanalyzer.state.StorageAnalyzerUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for the Files screen.
 *
 * Reuses [StorageAnalyzerRepository] directly — the Files screen
 * surfaces the same per-category media-store breakdown but in a
 * folder-browsing layout, so we get the live data for free without
 * duplicating the provider.
 */
class FilesViewModel(
    private val repository: StorageAnalyzerRepository,
) : ViewModel() {

    private val _uiState =
        MutableStateFlow<StorageAnalyzerUiState>(StorageAnalyzerUiState.Loading)
    val uiState: StateFlow<StorageAnalyzerUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    /** Force a fresh read; mirrors StorageAnalyzerViewModel.refresh(). */
    fun refresh() = load()

    private fun load() {
        viewModelScope.launch {
            _uiState.value = StorageAnalyzerUiState.Loading
            try {
                val result = repository.analyze()
                _uiState.value = StorageAnalyzerUiState.Success(result)
            } catch (t: Throwable) {
                _uiState.value = StorageAnalyzerUiState.Error(
                    t.message ?: "Failed to read folders",
                )
            }
        }
    }

    class Factory(
        private val repository: StorageAnalyzerRepository,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass.isAssignableFrom(FilesViewModel::class.java)) {
                "Unknown ViewModel class: ${modelClass.name}"
            }
            return FilesViewModel(repository) as T
        }
    }
}