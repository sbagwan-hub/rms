package com.tionix.rms.feature.history.data.repository

import com.tionix.rms.core.sync.data.local.PendingOperationDao
import com.tionix.rms.feature.history.data.remote.OperationsApiService
import com.tionix.rms.feature.history.domain.model.PendingOperationItem
import com.tionix.rms.feature.history.domain.model.SyncedOperationItem
import com.tionix.rms.feature.history.domain.repository.HistoryRepository
import com.tionix.rms.feature.sync.data.SyncScheduler
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
class HistoryRepositoryImpl @Inject constructor(
    private val pendingOperationDao: PendingOperationDao,
    private val operationsApiService: OperationsApiService,
    private val syncScheduler: SyncScheduler
) : HistoryRepository {

    override fun observePendingOperations(): Flow<List<PendingOperationItem>> {
        return pendingOperationDao.observePending().map { rows ->
            rows.map { entity ->
                PendingOperationItem(
                    clientOpId = entity.clientOpId,
                    type = entity.type,
                    createdAt = entity.createdAt,
                    state = entity.state,
                    lastError = entity.lastError
                )
            }
        }
    }

    override suspend fun getSyncedOperations(limit: Int): Result<List<SyncedOperationItem>> {
        return try {
            val response = operationsApiService.listOperations(mine = true, limit = limit, page = 1)
            if (!response.isSuccessful || response.body() == null) {
                return Result.failure(Exception("Failed to load synced operations (${response.code()})"))
            }
            Result.success(
                response.body()!!.map { dto ->
                    SyncedOperationItem(
                        id = dto.id,
                        type = dto.type,
                        status = dto.status,
                        performedAt = dto.performedAt,
                        summary = dto.summary,
                        reasonCode = dto.reasonCode,
                        boxId = dto.boxId,
                        boxBarcode = dto.boxBarcode,
                        fileId = dto.fileId,
                        fileBarcode = dto.fileBarcode
                    )
                }
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun triggerManualSync() {
        syncScheduler.scheduleImmediateSync()
    }
}
