package com.matox.nexcore.presentation.storageanalyzer.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.matox.nexcore.domain.repository.StorageAnalyzerRepository
import com.matox.nexcore.presentation.storageanalyzer.state.StorageAnalyzerUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class StorageAnalyzerViewModel(
    private val repository: StorageAnalyzerRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<StorageAnalyzerUiState>(StorageAnalyzerUiState.Loading)
    val uiState: StateFlow<StorageAnalyzerUiState> = _uiState.asStateFlow()

    init {
        analyze()
    }

    fun refresh() = analyze()

    private fun analyze() {
        viewModelScope.launch {
            _uiState.value = StorageAnalyzerUiState.Loading
            try {
                val result = repository.analyze()
                _uiState.value = StorageAnalyzerUiState.Success(result)
            } catch (t: Throwable) {
                _uiState.value =
                    StorageAnalyzerUiState.Error(t.message ?: "Failed to analyze storage")
            }
        }
    }

    class Factory(
        private val repository: StorageAnalyzerRepository,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass.isAssignableFrom(StorageAnalyzerViewModel::class.java)) {
                "Unknown ViewModel class: ${modelClass.name}"
            }
            return StorageAnalyzerViewModel(repository) as T
        }
    }
}
