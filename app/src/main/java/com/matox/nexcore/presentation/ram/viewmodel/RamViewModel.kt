package com.matox.nexcore.presentation.ram.viewmodel

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.matox.nexcore.data.device.AppIconLoader
import com.matox.nexcore.domain.model.AppRamUsage
import com.matox.nexcore.domain.repository.RamRepository
import com.matox.nexcore.presentation.ram.state.RamUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Exposes [RamUiState] for the RAM detail screen.
 *
 * Subscribes to [RamRepository.observeSnapshot] on construction. The
 * repository's underlying flow polls every 3 s, so the chart on the
 * detail screen ticks at the same cadence as the dashboard's metrics
 * row.
 *
 * Icons are loaded once per unique package and cached in the
 * ViewModel so the per-tick cost stays near zero. Off-main-thread
 * load via [Dispatchers.IO].
 */
class RamViewModel(
    private val repository: RamRepository,
    private val iconLoader: AppIconLoader? = null,
) : ViewModel() {

    private val _uiState = MutableStateFlow<RamUiState>(RamUiState.Loading)
    val uiState: StateFlow<RamUiState> = _uiState.asStateFlow()

    /** Cached icons — keyed by package name. Populated lazily as
     *  each new app appears in a poll result. */
    private val iconCache = HashMap<String, Bitmap>()

    init {
        observe()
    }

    /** No-op for now — the repo is already polling. Provided so the
     *  top-bar refresh button can be wired later without changing the
     *  VM surface. */
    fun refresh() {
        _uiState.value = _uiState.value
    }

    private fun observe() {
        repository.observeSnapshot()
            .onEach { snapshot ->
                val icons = if (iconLoader != null) {
                    loadIconsFor(snapshot.topApps)
                } else {
                    emptyMap()
                }
                _uiState.value = RamUiState.Success(
                    snapshot = snapshot,
                    appIcons = icons,
                )
            }
            .catch { t ->
                _uiState.value = RamUiState.Error(t.message ?: "Failed to read RAM")
            }
            .launchIn(viewModelScope)
    }

    /**
     * Resolve bitmaps for any apps that don't yet have an icon in
     * [iconCache]. Runs on [Dispatchers.IO] and only kicks off a
     * single launch per tick.
     */
    private fun loadIconsFor(apps: List<AppRamUsage>): Map<String, Bitmap> {
        val missing = apps.map { it.packageName }.filter { it !in iconCache }
        if (missing.isEmpty()) return iconCache.toMap()
        viewModelScope.launch {
            val loader = iconLoader ?: return@launch
            val newlyLoaded = withContext(Dispatchers.IO) {
                missing.mapNotNull { pkg ->
                    val bmp = loader.load(pkg) ?: return@mapNotNull null
                    pkg to bmp
                }.toMap()
            }
            iconCache.putAll(newlyLoaded)
            // Re-emit current success so the UI picks up the new icons.
            val current = _uiState.value
            if (current is RamUiState.Success) {
                _uiState.value = current.copy(appIcons = iconCache.toMap())
            }
        }
        return iconCache.toMap()
    }

    class Factory(
        private val repository: RamRepository,
        private val iconLoader: AppIconLoader? = null,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass.isAssignableFrom(RamViewModel::class.java)) {
                "Unknown ViewModel class: ${modelClass.name}"
            }
            return RamViewModel(repository, iconLoader) as T
        }
    }
}