package com.matox.nexcore.presentation.battery.viewmodel

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.matox.nexcore.data.device.AppIconLoader
import com.matox.nexcore.domain.model.BatteryAppUsage
import com.matox.nexcore.domain.repository.BatteryRepository
import com.matox.nexcore.presentation.battery.state.BatteryUiState
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
 * Exposes [BatteryUiState] for the Battery Monitor screen.
 *
 * Subscribes to [BatteryRepository.observeSnapshot] on construction.
 * The repository's underlying flow polls every 3 s, so the hero
 * ring, metric tiles, charts, and recommendations all tick at the
 * same cadence as the RAM detail screen.
 *
 * Icons are loaded once per unique package and cached in the
 * ViewModel so the per-tick cost stays near zero. Off-main-thread
 * load via [Dispatchers.IO].
 */
class BatteryViewModel(
    private val repository: BatteryRepository,
    private val iconLoader: AppIconLoader? = null,
) : ViewModel() {

    private val _uiState = MutableStateFlow<BatteryUiState>(BatteryUiState.Loading)
    val uiState: StateFlow<BatteryUiState> = _uiState.asStateFlow()

    /** Cached icons — keyed by package name. Populated lazily as
     *  each new app appears in a poll result. */
    private val iconCache = HashMap<String, Bitmap>()

    init {
        observe()
    }

    /** Force an immediate poll on the underlying repository so the
     *  hero ring, charts, and recommendations update without waiting
     *  for the next 3 s tick. */
    fun refresh() {
        viewModelScope.launch { repository.refreshNow() }
    }

    private fun observe() {
        repository.observeSnapshot()
            .onEach { snapshot ->
                val icons = if (iconLoader != null) {
                    loadIconsFor(snapshot.topApps)
                } else {
                    emptyMap()
                }
                _uiState.value = BatteryUiState.Success(
                    snapshot = snapshot,
                    appIcons = icons,
                )
            }
            .catch { t ->
                _uiState.value = BatteryUiState.Error(t.message ?: "Failed to read battery")
            }
            .launchIn(viewModelScope)
    }

    /**
     * Resolve bitmaps for any apps that don't yet have an icon in
     * [iconCache]. Runs on [Dispatchers.IO] and only kicks off a
     * single launch per tick.
     */
    private fun loadIconsFor(apps: List<BatteryAppUsage>): Map<String, Bitmap> {
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
            if (current is BatteryUiState.Success) {
                _uiState.value = current.copy(appIcons = iconCache.toMap())
            }
        }
        return iconCache.toMap()
    }

    class Factory(
        private val repository: BatteryRepository,
        private val iconLoader: AppIconLoader? = null,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass.isAssignableFrom(BatteryViewModel::class.java)) {
                "Unknown ViewModel class: ${modelClass.name}"
            }
            return BatteryViewModel(repository, iconLoader) as T
        }
    }
}
