package com.tionix.rms.core.sync.domain.repository

import com.tionix.rms.core.sync.domain.model.SyncOperation
import com.tionix.rms.core.sync.domain.model.SyncStatus

interface SyncRepository {
    suspend fun getSyncStatus(): Result<SyncStatus>
    suspend fun queueOperation(operation: SyncOperation): Result<Unit>
    suspend fun processPendingOperations(): Result<Unit>
    suspend fun retryFailedOperations(): Result<Unit>
    suspend fun clearCompletedOperations(): Result<Unit>
}
