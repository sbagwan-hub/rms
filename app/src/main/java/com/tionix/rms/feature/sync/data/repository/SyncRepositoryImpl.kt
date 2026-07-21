package com.tionix.rms.feature.sync.data.repository

import com.tionix.rms.core.sync.data.local.SyncOperationDao
import com.tionix.rms.core.sync.data.local.SyncOperationEntity
import com.tionix.rms.feature.sync.domain.model.PendingSyncQueue
import com.tionix.rms.feature.sync.domain.model.SyncItem
import com.tionix.rms.feature.sync.domain.model.SyncStatus
import com.tionix.rms.feature.sync.domain.repository.SyncRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

private fun SyncOperationEntity.toDomain(): SyncItem = SyncItem(
    id = id,
    actionType = type,
    data = payload,
    status = when (status) {
        "COMPLETED", "SYNCED" -> SyncStatus.SYNCED
        "FAILED" -> SyncStatus.FAILED
        else -> SyncStatus.PENDING
    },
    errorMessage = errorMessage,
    retryCount = retryCount,
    createdAt = createdAt,
    lastAttemptAt = null,
    syncedAt = null
)

/**
 * Bridges the real Room-backed queue (`core.sync` — what every feature repo
 * actually writes to) into the domain model `SyncWorker` consumes. Previously
 * this class had no DAO at all and always returned an empty queue, so
 * `SyncWorker` never saw any of the operations features were queuing.
 */
@Singleton
class SyncRepositoryImpl @Inject constructor(
    private val syncOperationDao: SyncOperationDao,
) : SyncRepository {

    override suspend fun getPendingSyncQueue(): Result<PendingSyncQueue> {
        return try {
            val pending = syncOperationDao.getPendingOperations().map { it.toDomain() }
            val failed = syncOperationDao.getFailedOperations().map { it.toDomain() }
            Result.success(
                PendingSyncQueue(
                    pendingItems = pending,
                    failedItems = failed,
                    pendingCount = pending.size,
                    failedCount = failed.size,
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun observePendingSyncQueue(): Flow<PendingSyncQueue> {
        // No reactive Flow query on SyncOperationDao today; callers needing
        // live updates should re-invoke getPendingSyncQueue() after a sync run.
        return flowOf(PendingSyncQueue(emptyList(), emptyList(), 0, 0))
    }

    override suspend fun retrySyncItem(itemId: String): Result<Unit> {
        return try {
            syncOperationDao.updateStatus(itemId, "PENDING")
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun retryAllFailedItems(): Result<Unit> {
        return try {
            syncOperationDao.getFailedOperations().forEach { syncOperationDao.updateStatus(it.id, "PENDING") }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteFailedItem(itemId: String): Result<Unit> {
        return try {
            syncOperationDao.delete(itemId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteAllFailedItems(): Result<Unit> {
        return try {
            syncOperationDao.getFailedOperations().forEach { syncOperationDao.delete(it.id) }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun markItemSynced(itemId: String): Result<Unit> {
        return try {
            syncOperationDao.updateStatus(itemId, "COMPLETED")
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun markItemFailed(itemId: String, errorMessage: String): Result<Unit> {
        return try {
            syncOperationDao.updateStatus(itemId, "FAILED")
            syncOperationDao.incrementRetryCount(itemId, errorMessage)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
