package com.tionix.rms.feature.sync.domain.usecase

import com.tionix.rms.feature.sync.domain.model.PendingSyncQueue
import com.tionix.rms.feature.sync.domain.repository.SyncRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetPendingSyncQueueUseCase @Inject constructor(
    private val repository: SyncRepository
) {
    suspend operator fun invoke(): Result<PendingSyncQueue> {
        return repository.getPendingSyncQueue()
    }
    
    fun observePendingSyncQueue(): Flow<PendingSyncQueue> {
        return repository.observePendingSyncQueue()
    }
}

class RetrySyncItemUseCase @Inject constructor(
    private val repository: SyncRepository
) {
    suspend operator fun invoke(itemId: String): Result<Unit> {
        return repository.retrySyncItem(itemId)
    }
}

class RetryAllFailedItemsUseCase @Inject constructor(
    private val repository: SyncRepository
) {
    suspend operator fun invoke(): Result<Unit> {
        return repository.retryAllFailedItems()
    }
}

class DeleteFailedItemUseCase @Inject constructor(
    private val repository: SyncRepository
) {
    suspend operator fun invoke(itemId: String): Result<Unit> {
        return repository.deleteFailedItem(itemId)
    }
}

class DeleteAllFailedItemsUseCase @Inject constructor(
    private val repository: SyncRepository
) {
    suspend operator fun invoke(): Result<Unit> {
        return repository.deleteAllFailedItems()
    }
}

class MarkItemSyncedUseCase @Inject constructor(
    private val repository: SyncRepository
) {
    suspend operator fun invoke(itemId: String): Result<Unit> {
        return repository.markItemSynced(itemId)
    }
}

class MarkItemFailedUseCase @Inject constructor(
    private val repository: SyncRepository
) {
    suspend operator fun invoke(itemId: String, errorMessage: String): Result<Unit> {
        return repository.markItemFailed(itemId, errorMessage)
    }
}
