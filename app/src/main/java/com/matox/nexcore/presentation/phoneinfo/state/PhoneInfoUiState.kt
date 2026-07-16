package com.matox.nexcore.presentation.phoneinfo.state

import com.matox.nexcore.domain.model.PhoneInfoSnapshot

/**
 * Top-level UI state for the Phone Info screen.
 *
 * - [Loading] is shown briefly on first launch while the synchronous
 *   snapshot read happens off the main thread.
 * - [Success] carries the immutable snapshot — every section card on the
 *   screen reads from it. The snapshot itself is computed once per
 *   `refresh()` so re-renders are cheap.
 * - [Error] is reserved for catastrophic failures (Provider threw even
 *   with runCatching). Should not be reachable in normal operation.
 */
sealed interface PhoneInfoUiState {
    data object Loading : PhoneInfoUiState
    data class Success(val snapshot: PhoneInfoSnapshot) : PhoneInfoUiState
    data class Error(val message: String) : PhoneInfoUiState
}
