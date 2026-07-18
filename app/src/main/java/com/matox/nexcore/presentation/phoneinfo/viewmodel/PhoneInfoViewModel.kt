package com.matox.nexcore.presentation.phoneinfo.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.matox.nexcore.domain.repository.PhoneInfoRepository
import com.matox.nexcore.presentation.phoneinfo.state.PhoneInfoUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PhoneInfoViewModel(
    private val repository: PhoneInfoRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<PhoneInfoUiState>(PhoneInfoUiState.Loading)
    val uiState: StateFlow<PhoneInfoUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    /**
     * Re-read every field of the snapshot. The provider wraps every
     * external call in `runCatching`, so this is safe to call repeatedly;
     * even if a single field throws, we still publish a `Success`.
     */
    fun refresh() {
        viewModelScope.launch {
            try {
                val snapshot = repository.snapshot()
                _uiState.value = PhoneInfoUiState.Success(snapshot)
            } catch (t: Throwable) {
                _uiState.value = PhoneInfoUiState.Error(t.message ?: "Failed to read phone info")
            }
        }
    }

    class Factory(
        private val repository: PhoneInfoRepository,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass.isAssignableFrom(PhoneInfoViewModel::class.java)) {
                "Unknown ViewModel class: ${modelClass.name}"
            }
            return PhoneInfoViewModel(repository) as T
        }
    }
}
