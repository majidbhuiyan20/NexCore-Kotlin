package com.matox.nexcore.domain.repository

import com.matox.nexcore.domain.model.StorageBreakdown

/**
 * Repository contract for the Storage Analyzer detail screen.
 * Implementation lives in `data/repository/`.
 */
interface StorageAnalyzerRepository {
    suspend fun analyze(): StorageBreakdown
}
