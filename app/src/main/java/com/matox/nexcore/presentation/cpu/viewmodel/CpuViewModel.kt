package com.matox.nexcore.presentation.cpu.viewmodel

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.matox.nexcore.data.device.AppIconLoader
import com.matox.nexcore.domain.model.CpuAppUsage
import com.matox.nexcore.domain.repository.CpuRepository
import com.matox.nexcore.presentation.cpu.state.CpuUiState
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
 * Exposes [CpuUiState] for the CPU Monitor screen.
 *
 * Subscribes to [CpuRepository.observeSnapshot] on construction. The
 * underlying flow polls every 1 s, so the hero ring, per-core bars,
 * and history chart all tick at "live" cadence.
 *
 * Icons are loaded once per unique package and cached in the ViewModel
 * so the per-tick cost stays near zero. Off-main-thread load via
 * [Dispatchers.IO].
 */
class CpuViewModel(
    private val repository: CpuRepository,
    private val iconLoader: AppIconLoader? = null,
) : ViewModel() {

    private val _uiState = MutableStateFlow<CpuUiState>(CpuUiState.Loading)
    val uiState: StateFlow<CpuUiState> = _uiState.asStateFlow()

    /** Cached icons — keyed by package name. */
    private val iconCache = HashMap<String, Bitmap>()

    init {
        observe()
    }

    /** Force an immediate poll on the underlying repository. */
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
                _uiState.value = CpuUiState.Success(
                    snapshot = snapshot,
                    appIcons = icons,
                )
            }
            .catch { t ->
                _uiState.value = CpuUiState.Error(t.message ?: "Failed to read CPU")
            }
            .launchIn(viewModelScope)
    }

    private fun loadIconsFor(apps: List<CpuAppUsage>): Map<String, Bitmap> {
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
            val current = _uiState.value
            if (current is CpuUiState.Success) {
                _uiState.value = current.copy(appIcons = iconCache.toMap())
            }
        }
        return iconCache.toMap()
    }

    class Factory(
        private val repository: CpuRepository,
        private val iconLoader: AppIconLoader? = null,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass.isAssignableFrom(CpuViewModel::class.java)) {
                "Unknown ViewModel class: ${modelClass.name}"
            }
            return CpuViewModel(repository, iconLoader) as T
        }
    }
}
