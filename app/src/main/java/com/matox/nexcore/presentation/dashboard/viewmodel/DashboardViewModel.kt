package com.matox.nexcore.presentation.dashboard.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.matox.nexcore.domain.repository.DashboardRepository
import com.matox.nexcore.presentation.dashboard.state.DashboardUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class DashboardViewModel(
    private val repository: DashboardRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<DashboardUiState>(DashboardUiState.Loading)
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        loadDashboard()
    }

    fun refresh() = loadDashboard()

    private fun loadDashboard() {
        viewModelScope.launch {
            _uiState.value = DashboardUiState.Loading
            repository.observeDashboardSnapshot()
                .catch { throwable ->
                    _uiState.value =
                        DashboardUiState.Error(throwable.message ?: "Unknown error")
                }
                .collect { snapshot ->
                    _uiState.value = DashboardUiState.Success(snapshot)
                }
        }
    }

    class Factory(
        private val repository: DashboardRepository,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass.isAssignableFrom(DashboardViewModel::class.java)) {
                "Unknown ViewModel class: ${modelClass.name}"
            }
            return DashboardViewModel(repository) as T
        }
    }
}