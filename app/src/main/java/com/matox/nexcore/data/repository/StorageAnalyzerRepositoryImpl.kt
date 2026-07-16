package com.matox.nexcore.data.repository

import com.matox.nexcore.data.datasource.StorageAnalyzerDataSource
import com.matox.nexcore.domain.model.StorageBreakdown
import com.matox.nexcore.domain.repository.StorageAnalyzerRepository

class StorageAnalyzerRepositoryImpl(
    private val dataSource: StorageAnalyzerDataSource,
) : StorageAnalyzerRepository {

    override suspend fun analyze(): StorageBreakdown {
        // First (and only) emission from the source.
        var result: StorageBreakdown? = null
        dataSource.analyze().collect { result = it }
        return result ?: StorageBreakdown(
            internalUsedGb = 0f,
            internalTotalGb = 0f,
            categories = emptyList(),
            largeFiles = emptyList(),
            insights = com.matox.nexcore.domain.model.StorageInsights(
                largeFilesCount = 0,
                largeFilesGb = 0f,
                duplicateCount = 0,
                duplicateGb = 0f,
                emptyFolders = 0,
                oldFilesCount = 0,
                oldFilesGb = 0f,
            ),
        )
    }
}
