package com.tionix.rms.feature.sync.data.repository

import com.tionix.rms.core.sync.data.local.PendingOperationDao
import com.tionix.rms.core.sync.data.local.PendingOperationEntity
import com.tionix.rms.feature.sync.domain.model.PendingSyncQueue
import com.tionix.rms.feature.sync.domain.model.SyncItem
import com.tionix.rms.feature.sync.domain.model.SyncStatus
import com.tionix.rms.feature.sync.domain.repository.SyncRepository
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
    timeZone = TimeZone.getTimeZone("UTC")
}

private fun PendingOperationEntity.toDomain(): SyncItem = SyncItem(
    id = clientOpId,
    actionType = type,
    data = payloadJson,
    status = when (state) {
        "FAILED" -> SyncStatus.FAILED
        "SENDING" -> SyncStatus.PENDING
        else -> SyncStatus.PENDING
    },
    errorMessage = lastError,
    retryCount = attemptCount,
    createdAt = dateFormat.format(Date(createdAt)),
    lastAttemptAt = null,
    syncedAt = null
)

@Singleton
class SyncRepositoryImpl @Inject constructor(
    private val pendingOperationDao: PendingOperationDao,
) : SyncRepository {

    override suspend fun getPendingSyncQueue(): Result<PendingSyncQueue> {
        return try {
            val pending = pendingOperationDao.getPending()
                .filter { it.state == "QUEUED" }
                .map { it.toDomain() }
            val failed = pendingOperationDao.getPending()
                .filter { it.state == "FAILED" }
                .map { it.toDomain() }
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
        return pendingOperationDao.observePending().map { list ->
            val pending = list.filter { it.state == "QUEUED" || it.state == "SENDING" }.map { it.toDomain() }
            val failed = list.filter { it.state == "FAILED" }.map { it.toDomain() }
            PendingSyncQueue(
                pendingItems = pending,
                failedItems = failed,
                pendingCount = pending.size,
                failedCount = failed.size,
            )
        }
    }

    override suspend fun retrySyncItem(itemId: String): Result<Unit> {
        return try {
            val entity = pendingOperationDao.getPending().firstOrNull { it.clientOpId == itemId }
                ?: return Result.failure(IllegalArgumentException("Sync item not found"))
            pendingOperationDao.updateState(itemId, "QUEUED", entity.lastError, entity.attemptCount)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun retryAllFailedItems(): Result<Unit> {
        return try {
            pendingOperationDao.getPending()
                .filter { it.state == "FAILED" }
                .forEach { pendingOperationDao.updateState(it.clientOpId, "QUEUED", it.lastError, it.attemptCount) }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteFailedItem(itemId: String): Result<Unit> {
        return try {
            pendingOperationDao.delete(itemId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteAllFailedItems(): Result<Unit> {
        return try {
            pendingOperationDao.getPending()
                .filter { it.state == "FAILED" }
                .forEach { pendingOperationDao.delete(it.clientOpId) }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun markItemSynced(itemId: String): Result<Unit> {
        return try {
            pendingOperationDao.delete(itemId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun markItemFailed(itemId: String, errorMessage: String): Result<Unit> {
        return try {
            val entity = pendingOperationDao.getPending().firstOrNull { it.clientOpId == itemId }
            pendingOperationDao.updateState(
                clientOpId = itemId,
                state = "FAILED",
                error = errorMessage,
                attemptCount = entity?.attemptCount ?: 0
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
