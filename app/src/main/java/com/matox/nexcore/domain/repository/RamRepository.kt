package com.matox.nexcore.domain.repository

import com.matox.nexcore.domain.model.RamSnapshot
import kotlinx.coroutines.flow.Flow

/**
 * Repository contract for the RAM detail screen.
 *
 * [observeSnapshot] emits a fresh [RamSnapshot] on each call. The
 * implementation polls the underlying providers so the chart on the
 * detail screen can keep ticking at the same cadence as the
 * dashboard metrics row (3 s).
 */
interface RamRepository {
    fun observeSnapshot(): Flow<RamSnapshot>
}