package com.matox.nexcore.presentation.appmanager.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.matox.nexcore.domain.model.AppFilterTab
import com.matox.nexcore.domain.model.AppInfo
import com.matox.nexcore.domain.model.AppSort
import com.matox.nexcore.domain.repository.AppManagerRepository
import com.matox.nexcore.presentation.appmanager.state.AppManagerUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class AppManagerViewModel(
    private val repository: AppManagerRepository,
) : ViewModel() {

    /** Raw list of all installed apps (icon already loaded). */
    private val allApps = MutableStateFlow<List<AppInfo>>(emptyList())

    /** Set to `true` while painting from the disk cache so the UI can
     *  display a subtle overlay without blanking the list. */
    private val isFromCache = MutableStateFlow(false)

    private val search = MutableStateFlow("")
    private val sort = MutableStateFlow(AppSort.NAME_ASC)
    private val tab = MutableStateFlow(AppFilterTab.ALL)

    private val _uiState = MutableStateFlow<AppManagerUiState>(AppManagerUiState.Loading)
    val uiState: StateFlow<AppManagerUiState> = _uiState.asStateFlow()

    init {
        loadApps()
        observeInputs()
    }

    fun setSearch(query: String) {
        search.value = query
    }

    fun setSort(newSort: AppSort) {
        sort.value = newSort
    }

    fun setTab(newTab: AppFilterTab) {
        tab.value = newTab
    }

    fun refresh() = loadApps()

    private fun loadApps() {
        repository.observeApps()
            .catch { t ->
                _uiState.value = AppManagerUiState.Error(t.message ?: "Failed to load apps")
            }
            .onEach { (apps, fromCache) ->
                allApps.value = apps
                isFromCache.value = fromCache
            }
            .launchIn(viewModelScope)
    }

    private fun observeInputs() {
        combine(allApps, search, sort, tab, isFromCache) { apps, q, s, t, fromCache ->
            val qLower = q.trim().lowercase()
            val filtered = apps
                .asSequence()
                .filter { app ->
                    val tabOk = when (t) {
                        AppFilterTab.ALL -> true
                        AppFilterTab.USER -> !app.isSystem
                        AppFilterTab.SYSTEM -> app.isSystem
                    }
                    val searchOk = qLower.isEmpty() ||
                        app.displayName.lowercase().contains(qLower) ||
                        app.packageName.lowercase().contains(qLower)
                    tabOk && searchOk
                }
                .toList()
            val sorted = filtered.sortedWith(comparator(s))
            AppManagerUiState.Success(
                filtered = sorted,
                allApps = apps,
                search = q,
                sort = s,
                tab = t,
                isFromCache = fromCache,
            )
        }
            .catch { t ->
                _uiState.value = AppManagerUiState.Error(t.message ?: "Failed to combine state")
            }
            .onEach { _uiState.value = it }
            .launchIn(viewModelScope)
    }

    private fun comparator(s: AppSort): Comparator<AppInfo> = when (s) {
        AppSort.NAME_ASC -> compareBy { it.displayName.lowercase() }
        AppSort.NAME_DESC -> compareByDescending { it.displayName.lowercase() }
        AppSort.SIZE_DESC -> compareByDescending { it.sizeBytes }
        AppSort.SIZE_ASC -> compareBy { it.sizeBytes }
        AppSort.DATE_DESC -> compareByDescending { it.lastUpdatedEpochMs }
        AppSort.DATE_ASC -> compareBy { it.lastUpdatedEpochMs }
    }

    class Factory(
        private val repository: AppManagerRepository,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass.isAssignableFrom(AppManagerViewModel::class.java)) {
                "Unknown ViewModel class: ${modelClass.name}"
            }
            return AppManagerViewModel(repository) as T
        }
    }
}
