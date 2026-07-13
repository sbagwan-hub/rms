package com.tionix.rms.feature.sync.data.repository

import com.tionix.rms.feature.sync.domain.model.PendingSyncQueue
import com.tionix.rms.feature.sync.domain.repository.SyncRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncRepositoryImpl @Inject constructor() : SyncRepository {
    private val emptyQueue = PendingSyncQueue(
        pendingItems = emptyList(),
        failedItems = emptyList(),
        pendingCount = 0,
        failedCount = 0
    )

    override suspend fun getPendingSyncQueue(): Result<PendingSyncQueue> {
        return Result.success(emptyQueue)
    }

    override fun observePendingSyncQueue(): Flow<PendingSyncQueue> {
        return flowOf(emptyQueue)
    }

    override suspend fun retrySyncItem(itemId: String): Result<Unit> {
        return Result.success(Unit)
    }

    override suspend fun retryAllFailedItems(): Result<Unit> {
        return Result.success(Unit)
    }

    override suspend fun deleteFailedItem(itemId: String): Result<Unit> {
        return Result.success(Unit)
    }

    override suspend fun deleteAllFailedItems(): Result<Unit> {
        return Result.success(Unit)
    }

    override suspend fun markItemSynced(itemId: String): Result<Unit> {
        return Result.success(Unit)
    }

    override suspend fun markItemFailed(itemId: String, errorMessage: String): Result<Unit> {
        return Result.success(Unit)
    }
}
