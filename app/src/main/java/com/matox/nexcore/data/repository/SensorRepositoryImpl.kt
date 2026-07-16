package com.matox.nexcore.data.repository

import com.matox.nexcore.data.device.SensorProvider
import com.matox.nexcore.domain.model.SensorSnapshot
import com.matox.nexcore.domain.repository.SensorRepository
import kotlinx.coroutines.flow.Flow

/**
 * Repository over [SensorProvider].
 *
 * Thin pass-through — the provider owns:
 *  - the `SensorManager` enumeration (one-shot, on construction),
 *  - the `SensorEventListener` registration (driven by the
 *    host's lifecycle),
 *  - the throttled snapshot emission (~10 Hz),
 *  - the high-frequency motion side-channel used by the hero
 *    card.
 *
 * This repository exists for two reasons:
 *  1. Symmetry with the other features (battery / RAM / wifi /
 *     storage) which all sit behind a repository so the
 *     ViewModel only depends on the interface.
 *  2. To expose [refreshNow] and [stopStreaming] in a way the
 *     ViewModel can drive — the provider's [SensorProvider.refresh]
 *     pushes the latest cache into the StateFlow immediately,
 *     and [stopStreaming] tears down listeners even if the
 *     ViewModel is recreated (e.g. process death recovery).
 */
class SensorRepositoryImpl(
    private val provider: SensorProvider,
) : SensorRepository {

    override fun observeSnapshot(): Flow<SensorSnapshot> = provider.snapshot

    override suspend fun refreshNow() {
        runCatching { provider.refresh() }
    }

    override fun stopStreaming() {
        runCatching { provider.unregister() }
    }
}