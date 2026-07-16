package com.matox.nexcore.presentation.appmanager.state

import com.matox.nexcore.domain.model.AppFilterTab
import com.matox.nexcore.domain.model.AppInfo
import com.matox.nexcore.domain.model.AppSort

/**
 * Top-level UI state for the App Manager screen.
 *
 * `Loading` is only shown when there's no cached snapshot AND no live
 * data yet. The first paint almost always comes from [AppManagerRepository]'s
 * disk cache, which transitions straight into `Success` with `isFromCache = true`.
 * Once the live snapshot arrives the state flips to `isFromCache = false` but
 * stays in `Success` so the screen never blanks.
 */
sealed interface AppManagerUiState {

    data object Loading : AppManagerUiState

    data class Success(
        val filtered: List<AppInfo>,
        val allApps: List<AppInfo>,
        val search: String,
        val sort: AppSort,
        val tab: AppFilterTab,
        /** True while the list is being painted from disk; the screen
         *  can render a soft shimmer overlay without blocking. */
        val isFromCache: Boolean = false,
    ) : AppManagerUiState {
        val totalCount: Int get() = allApps.size
        val userCount: Int get() = allApps.count { !it.isSystem }
        val systemCount: Int get() = allApps.count { it.isSystem }
        val totalSizeBytes: Long get() = allApps.sumOf { it.sizeBytes }
    }

    data class Error(val message: String) : AppManagerUiState
}

