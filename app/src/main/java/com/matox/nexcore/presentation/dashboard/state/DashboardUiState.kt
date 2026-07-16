package com.matox.nexcore.presentation.dashboard.state

import com.matox.nexcore.domain.model.DashboardSnapshot

/**
 * UI state for the dashboard screen.
 *
 * - [Loading]  : initial fetch in progress
 * - [Success]  : data ready to render
 * - [Error]    : something went wrong
 */
sealed interface DashboardUiState {
    data object Loading : DashboardUiState
    data class Success(val snapshot: DashboardSnapshot) : DashboardUiState
    data class Error(val message: String) : DashboardUiState
}