package com.tionix.rms.core.sync.data.repository

import android.content.Context
import com.tionix.rms.core.sync.data.local.SyncDatabase
import com.tionix.rms.core.sync.data.local.SyncOperationEntity
import com.tionix.rms.core.sync.data.local.toDomain
import com.tionix.rms.core.sync.data.local.toEntity
import com.tionix.rms.core.sync.domain.model.SyncOperation
import com.tionix.rms.core.sync.domain.model.SyncOperationStatus
import com.tionix.rms.core.sync.domain.model.SyncStatus
import com.tionix.rms.core.sync.domain.repository.SyncRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val syncDatabase: SyncDatabase
) : SyncRepository {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    override suspend fun getSyncStatus(): Result<SyncStatus> {
        return withContext(Dispatchers.IO) {
            try {
                val pendingCount = syncDatabase.syncOperationDao().getPendingCount()
                val failedCount = syncDatabase.syncOperationDao().getFailedCount()
                
                Result.success(
                    SyncStatus(
                        isSyncing = pendingCount > 0,
                        lastSyncTime = getLastSyncTime(),
                        pendingOperations = pendingCount,
                        failedOperations = failedCount
                    )
                )
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    override suspend fun queueOperation(operation: SyncOperation): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                syncDatabase.syncOperationDao().insert(operation.toEntity())
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    override suspend fun processPendingOperations(): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val pendingOps = syncDatabase.syncOperationDao().getPendingOperations()
                
                pendingOps.forEach { entity ->
                    syncDatabase.syncOperationDao().updateStatus(entity.id, SyncOperationStatus.IN_PROGRESS.name)
                    
                    // Simulate network call - in production, this would make actual API calls
                    val success = simulateNetworkCall(entity)
                    
                    if (success) {
                        syncDatabase.syncOperationDao().updateStatus(entity.id, SyncOperationStatus.COMPLETED.name)
                    } else {
                        syncDatabase.syncOperationDao().updateStatus(entity.id, SyncOperationStatus.FAILED.name)
                        syncDatabase.syncOperationDao().incrementRetryCount(entity.id, "Network error")
                    }
                }
                
                syncDatabase.syncOperationDao().clearCompleted()
                saveLastSyncTime()
                
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    override suspend fun retryFailedOperations(): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val failedOps = syncDatabase.syncOperationDao().getFailedOperations()
                
                failedOps.forEach { entity ->
                    syncDatabase.syncOperationDao().updateStatus(entity.id, SyncOperationStatus.PENDING.name)
                }
                
                processPendingOperations()
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    override suspend fun clearCompletedOperations(): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                syncDatabase.syncOperationDao().clearCompleted()
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    private fun simulateNetworkCall(entity: SyncOperationEntity): Boolean {
        // In production, this would make actual HTTP calls to the backend
        // For now, simulate success for demonstration
        return true
    }

    private fun getLastSyncTime(): String? {
        val prefs = context.getSharedPreferences("sync_prefs", Context.MODE_PRIVATE)
        return prefs.getString("last_sync_time", null)
    }

    private fun saveLastSyncTime() {
        val prefs = context.getSharedPreferences("sync_prefs", Context.MODE_PRIVATE)
        prefs.edit().putString("last_sync_time", dateFormat.format(Date())).apply()
    }
}

private fun SyncOperation.toEntity(): SyncOperationEntity {
    return SyncOperationEntity(
        id = id,
        type = type.name,
        endpoint = endpoint,
        payload = payload,
        status = status.name,
        retryCount = retryCount,
        createdAt = createdAt,
        errorMessage = errorMessage
    )
}

private fun SyncOperationEntity.toDomain(): SyncOperation {
    return SyncOperation(
        id = id,
        type = com.tionix.rms.core.sync.domain.model.SyncOperationType.valueOf(type),
        endpoint = endpoint,
        payload = payload,
        status = SyncOperationStatus.valueOf(status),
        retryCount = retryCount,
        createdAt = createdAt,
        errorMessage = errorMessage
    )
}
