package com.tionix.rms.core.sync.domain.usecase

import com.tionix.rms.core.sync.domain.model.SyncStatus
import com.tionix.rms.core.sync.domain.repository.SyncRepository
import javax.inject.Inject

class GetSyncStatusUseCase @Inject constructor(private val repository: SyncRepository) {
    suspend operator fun invoke(): Result<SyncStatus> {
        return repository.getSyncStatus()
    }
}

class QueueOperationUseCase @Inject constructor(private val repository: SyncRepository) {
    suspend operator fun invoke(operation: com.tionix.rms.core.sync.domain.model.SyncOperation): Result<Unit> {
        return repository.queueOperation(operation)
    }
}

class SyncNowUseCase @Inject constructor(private val repository: SyncRepository) {
    suspend operator fun invoke(): Result<Unit> {
        return repository.processPendingOperations()
    }
}

class RetryFailedOperationsUseCase @Inject constructor(private val repository: SyncRepository) {
    suspend operator fun invoke(): Result<Unit> {
        return repository.retryFailedOperations()
    }
}
