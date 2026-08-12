package com.tionix.rms.feature.history.domain.repository

import com.tionix.rms.feature.history.domain.model.PendingOperationItem
import com.tionix.rms.feature.history.domain.model.SyncedOperationItem
import kotlinx.coroutines.flow.Flow

interface HistoryRepository {
    fun observePendingOperations(): Flow<List<PendingOperationItem>>
    suspend fun getSyncedOperations(limit: Int = 50): Result<List<SyncedOperationItem>>
    suspend fun triggerManualSync()
}
