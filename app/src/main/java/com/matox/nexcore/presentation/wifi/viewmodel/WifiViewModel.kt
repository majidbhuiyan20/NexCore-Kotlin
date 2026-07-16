package com.matox.nexcore.presentation.wifi.viewmodel

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.matox.nexcore.data.device.AppIconLoader
import com.matox.nexcore.domain.model.AppTrafficRow
import com.matox.nexcore.domain.repository.WifiRepository
import com.matox.nexcore.presentation.wifi.state.WifiUiState
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
 * Exposes [WifiUiState] for the WiFi detail screen.
 *
 * Subscribes to [WifiRepository.observeSnapshot] on construction.
 * The repository's underlying flow polls every 3 s, so the hero ring,
 * IP rows, and app-traffic list all tick at the same cadence as the
 * RAM / Battery detail screens.
 *
 * Icons are loaded once per unique package and cached in the
 * ViewModel so the per-tick cost stays near zero. Off-main-thread
 * load via [Dispatchers.IO].
 */
class WifiViewModel(
    private val repository: WifiRepository,
    private val iconLoader: AppIconLoader? = null,
) : ViewModel() {

    private val _uiState = MutableStateFlow<WifiUiState>(WifiUiState.Loading)
    val uiState: StateFlow<WifiUiState> = _uiState.asStateFlow()

    /** Cached icons — keyed by package name. Populated lazily as
     *  each new app appears in a poll result. */
    private val iconCache = HashMap<String, Bitmap>()

    init {
        observe()
    }

    /** Force an immediate poll on the underlying repository so the
     *  hero ring, IP rows, and app-traffic chart update without
     *  waiting for the next 3 s tick. */
    fun refresh() {
        viewModelScope.launch { repository.refreshNow() }
    }

    private fun observe() {
        repository.observeSnapshot()
            .onEach { snapshot ->
                val icons = if (iconLoader != null) {
                    loadIconsFor(snapshot.appTraffic)
                } else {
                    emptyMap()
                }
                _uiState.value = WifiUiState.Success(
                    snapshot = snapshot,
                    appIcons = icons,
                )
            }
            .catch { t ->
                _uiState.value = WifiUiState.Error(t.message ?: "Failed to read WiFi state")
            }
            .launchIn(viewModelScope)
    }

    /**
     * Resolve bitmaps for any apps that don't yet have an icon in
     * [iconCache]. Runs on [Dispatchers.IO] and only kicks off a
     * single launch per tick.
     */
    private fun loadIconsFor(apps: List<AppTrafficRow>): Map<String, Bitmap> {
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
            if (current is WifiUiState.Success) {
                _uiState.value = current.copy(appIcons = iconCache.toMap())
            }
        }
        return iconCache.toMap()
    }

    class Factory(
        private val repository: WifiRepository,
        private val iconLoader: AppIconLoader? = null,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass.isAssignableFrom(WifiViewModel::class.java)) {
                "Unknown ViewModel class: ${modelClass.name}"
            }
            return WifiViewModel(repository, iconLoader) as T
        }
    }
}