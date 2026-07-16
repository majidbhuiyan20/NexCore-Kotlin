package com.matox.nexcore.presentation.datausage.viewmodel

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.matox.nexcore.data.device.AppIconLoader
import com.matox.nexcore.domain.model.AppDataUsage
import com.matox.nexcore.domain.repository.DataUsageRepository
import com.matox.nexcore.presentation.datausage.state.DataUsageUiState
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
 * Exposes [DataUsageUiState] for the Data Usage Monitor screen.
 *
 * Subscribes to [DataUsageRepository.observeSnapshot] on construction.
 * The underlying flow polls every 5 s — enough for the metrics tiles
 * and the per-app ranking without thrashing the system counters.
 */
class DataUsageViewModel(
    private val repository: DataUsageRepository,
    private val iconLoader: AppIconLoader? = null,
) : ViewModel() {

    private val _uiState = MutableStateFlow<DataUsageUiState>(DataUsageUiState.Loading)
    val uiState: StateFlow<DataUsageUiState> = _uiState.asStateFlow()

    /** Cached icons — keyed by package name. */
    private val iconCache = HashMap<String, Bitmap>()

    init {
        observe()
    }

    fun refresh() {
        viewModelScope.launch { repository.refreshNow() }
    }

    private fun observe() {
        repository.observeSnapshot()
            .onEach { snapshot ->
                val icons = if (iconLoader != null) {
                    loadIconsFor(snapshot.perApp)
                } else {
                    emptyMap()
                }
                _uiState.value = DataUsageUiState.Success(
                    snapshot = snapshot,
                    appIcons = icons,
                )
            }
            .catch { t ->
                _uiState.value = DataUsageUiState.Error(t.message ?: "Failed to read data usage")
            }
            .launchIn(viewModelScope)
    }

    private fun loadIconsFor(apps: List<AppDataUsage>): Map<String, Bitmap> {
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
            if (current is DataUsageUiState.Success) {
                _uiState.value = current.copy(appIcons = iconCache.toMap())
            }
        }
        return iconCache.toMap()
    }

    class Factory(
        private val repository: DataUsageRepository,
        private val iconLoader: AppIconLoader? = null,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass.isAssignableFrom(DataUsageViewModel::class.java)) {
                "Unknown ViewModel class: ${modelClass.name}"
            }
            return DataUsageViewModel(repository, iconLoader) as T
        }
    }
}
