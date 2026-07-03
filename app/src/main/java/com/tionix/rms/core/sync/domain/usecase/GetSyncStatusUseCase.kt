package com.tionix.rms.core.sync.domain.usecase

import com.tionix.rms.core.sync.domain.model.SyncStatus
import com.tionix.rms.core.sync.domain.repository.SyncRepository

class GetSyncStatusUseCase(private val repository: SyncRepository) {
    suspend operator fun invoke(): Result<SyncStatus> {
        return repository.getSyncStatus()
    }
}

class QueueOperationUseCase(private val repository: SyncRepository) {
    suspend operator fun invoke(operation: com.tionix.rms.core.sync.domain.model.SyncOperation): Result<Unit> {
        return repository.queueOperation(operation)
    }
}

class SyncNowUseCase(private val repository: SyncRepository) {
    suspend operator fun invoke(): Result<Unit> {
        return repository.processPendingOperations()
    }
}

class RetryFailedOperationsUseCase(private val repository: SyncRepository) {
    suspend operator fun invoke(): Result<Unit> {
        return repository.retryFailedOperations()
    }
}
