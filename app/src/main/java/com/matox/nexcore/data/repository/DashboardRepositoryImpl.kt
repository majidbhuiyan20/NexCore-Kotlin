package com.matox.nexcore.data.repository

import com.matox.nexcore.data.datasource.DashboardLocalDataSource
import com.matox.nexcore.domain.model.DashboardSnapshot
import com.matox.nexcore.domain.repository.DashboardRepository
import kotlinx.coroutines.flow.Flow

class DashboardRepositoryImpl(
    private val localDataSource: DashboardLocalDataSource,
) : DashboardRepository {

    override fun observeDashboardSnapshot(): Flow<DashboardSnapshot> =
        localDataSource.snapshot()
}