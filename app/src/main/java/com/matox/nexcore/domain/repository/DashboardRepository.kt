package com.matox.nexcore.domain.repository

import com.matox.nexcore.domain.model.DashboardSnapshot
import kotlinx.coroutines.flow.Flow

/**
 * Repository contract for fetching dashboard data.
 * Implementations live in the data layer.
 */
interface DashboardRepository {
    fun observeDashboardSnapshot(): Flow<DashboardSnapshot>
}