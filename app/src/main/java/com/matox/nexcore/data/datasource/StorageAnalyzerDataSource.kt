package com.matox.nexcore.data.datasource

import com.matox.nexcore.data.device.StorageAnalyzerProvider
import com.matox.nexcore.domain.model.StorageBreakdown
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

/**
 * Source for Storage Analyzer payloads. The single real
 * implementation is [LiveStorageAnalyzerDataSource]; the interface
 * exists to keep the repository layer unaware of the provider's
 * internals and to make unit tests easy.
 */
interface StorageAnalyzerDataSource {
    fun analyze(): Flow<StorageBreakdown>
}

class LiveStorageAnalyzerDataSource(
    private val provider: StorageAnalyzerProvider,
) : StorageAnalyzerDataSource {

    override fun analyze(): Flow<StorageBreakdown> = flow {
        emit(provider.analyze())
    }.flowOn(Dispatchers.IO)
}
