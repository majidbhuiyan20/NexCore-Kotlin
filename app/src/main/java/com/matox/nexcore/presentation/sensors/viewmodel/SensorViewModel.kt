package com.matox.nexcore.presentation.sensors.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.matox.nexcore.data.device.SensorProvider
import com.matox.nexcore.domain.model.SensorLiveMotion
import com.matox.nexcore.domain.repository.SensorRepository
import com.matox.nexcore.presentation.sensors.state.SensorUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

/**
 * Exposes [SensorUiState] for the Sensor Monitor screen.
 *
 * Lifecycle:
 *  - [init] subscribes to [SensorRepository.observeSnapshot] and
 *    to the provider's motion side-channel.
 *  - [onStart] / [onStop] are called by the screen composable
 *    using a `LifecycleEventObserver` — they register / unregister
 *    the underlying SensorEventListener so battery isn't drained
 *    when the screen isn't visible.
 *  - [refresh] forces an immediate re-publish of the current
 *    snapshot via [SensorRepository.refreshNow].
 *  - [release] is the explicit "tear down everything" path —
 *    stop streaming. The host normally doesn't need to call this
 *    (the ViewModel is cancelled automatically on `onCleared`),
 *    but it's exposed for tests / previews.
 */
class SensorViewModel(
    private val repository: SensorRepository,
    private val provider: SensorProvider,
) : ViewModel() {

    private val _uiState = MutableStateFlow<SensorUiState>(SensorUiState.Loading)
    val uiState: StateFlow<SensorUiState> = _uiState.asStateFlow()

    private var streaming: Boolean = false

    init {
        observeSnapshot()
        observeMotion()
    }

    /**
     * Subscribe to the static sensor snapshot. Catches any
     * thrown exception from the underlying flow and surfaces it
     * as [SensorUiState.Error].
     */
    private fun observeSnapshot() {
        repository.observeSnapshot()
            .onEach { snapshot ->
                val current = _uiState.value
                val motion = if (current is SensorUiState.Success) {
                    current.liveMotion
                } else {
                    SensorLiveMotion.Empty
                }
                _uiState.value = SensorUiState.Success(
                    snapshot = snapshot,
                    liveMotion = motion,
                )
            }
            .catch { t ->
                _uiState.value = SensorUiState.Error(
                    t.message ?: "Failed to read sensors",
                )
            }
            .launchIn(viewModelScope)
    }

    /**
     * Subscribe to the provider's high-frequency motion feed.
     * The hero card uses this so the ring animation can pulse
     * independently of the full snapshot's 10 Hz throttle.
     */
    private fun observeMotion() {
        provider.motion
            .onEach { motion ->
                val current = _uiState.value
                if (current is SensorUiState.Success) {
                    _uiState.value = current.copy(liveMotion = motion)
                }
            }
            .launchIn(viewModelScope)
    }

    /** Hook for the screen — call from `Lifecycle.Event.ON_START`. */
    fun onStart() {
        if (streaming) return
        streaming = true
        runCatching { provider.register() }
    }

    /** Hook for the screen — call from `Lifecycle.Event.ON_STOP`. */
    fun onStop() {
        if (!streaming) return
        streaming = false
        runCatching { provider.unregister() }
    }

    /** Manual refresh — re-publishes the latest snapshot. */
    fun refresh() {
        viewModelScope.launch { repository.refreshNow() }
    }

    /**
     * Tear down listeners. Idempotent. Called by [onCleared] to
     * keep the explicit-release contract obvious.
     */
    fun release() {
        onStop()
        repository.stopStreaming()
    }

    override fun onCleared() {
        super.onCleared()
        release()
    }

    class Factory(
        private val repository: SensorRepository,
        private val provider: SensorProvider,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass.isAssignableFrom(SensorViewModel::class.java)) {
                "Unknown ViewModel class: ${modelClass.name}"
            }
            return SensorViewModel(repository, provider) as T
        }
    }
}