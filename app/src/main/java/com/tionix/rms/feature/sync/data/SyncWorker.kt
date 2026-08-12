package com.tionix.rms.feature.sync.data

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.tionix.rms.core.sync.data.local.PendingOperationDao
import com.tionix.rms.feature.sync.data.remote.SyncApiService
import com.tionix.rms.feature.sync.data.remote.dto.SyncOperationDto
import com.tionix.rms.feature.sync.data.remote.dto.SyncOperationsRequestDto
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.io.IOException
import retrofit2.HttpException

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val pendingOperationDao: PendingOperationDao,
    private val syncApiService: SyncApiService,
) : CoroutineWorker(context, params) {

    companion object {
        const val WORK_NAME = "sync_worker"
        private const val BATCH_SIZE = 50
        private const val MAX_ATTEMPTS = 5
    }

    private val gson = Gson()

    override suspend fun doWork(): Result {
        val pending = pendingOperationDao.getPending()
        if (pending.isEmpty()) {
            return Result.success()
        }

        var retryableFailure = false

        pending.chunked(BATCH_SIZE).forEach { batch ->
            val operations = batch.map { entity ->
                val payloadType = object : TypeToken<Map<String, Any?>>() {}.type
                val payload: Map<String, Any?> = gson.fromJson(entity.payloadJson, payloadType)
                SyncOperationDto(type = entity.type, payload = payload)
            }

            try {
                val response = syncApiService.syncOperations(SyncOperationsRequestDto(operations))
                if (!response.isSuccessful || response.body() == null) {
                    if (response.code() in 400..499) {
                        batch.forEach { markFailed(it.clientOpId, response.message(), it.attemptCount) }
                    } else {
                        batch.forEach { markRetryable(it) }
                        retryableFailure = true
                    }
                    return@forEach
                }

                val results = response.body()!!
                val resultById = results.associateBy { it.clientOpId }

                batch.forEach { entity ->
                    val result = resultById[entity.clientOpId]
                    when (result?.status) {
                        "ok", "duplicate" -> pendingOperationDao.delete(entity.clientOpId)
                        "rejected" -> markFailed(
                            entity.clientOpId,
                            result.error ?: "Rejected by server",
                            entity.attemptCount
                        )
                        else -> {
                            markRetryable(entity)
                            retryableFailure = true
                        }
                    }
                }
            } catch (error: IOException) {
                batch.forEach { markRetryable(it) }
                retryableFailure = true
            } catch (error: HttpException) {
                if (error.code() in 400..499) {
                    batch.forEach { markFailed(it.clientOpId, error.message(), it.attemptCount) }
                } else {
                    batch.forEach { markRetryable(it) }
                    retryableFailure = true
                }
            }
        }

        return if (retryableFailure) Result.retry() else Result.success()
    }

    private suspend fun markRetryable(entity: com.tionix.rms.core.sync.data.local.PendingOperationEntity) {
        val nextAttempt = entity.attemptCount + 1
        if (nextAttempt > MAX_ATTEMPTS) {
            markFailed(entity.clientOpId, entity.lastError ?: "Max retry attempts reached", entity.attemptCount)
        } else {
            pendingOperationDao.updateState(
                clientOpId = entity.clientOpId,
                state = "QUEUED",
                error = entity.lastError,
                attemptCount = nextAttempt
            )
        }
    }

    private suspend fun markFailed(clientOpId: String, error: String, attemptCount: Int) {
        pendingOperationDao.updateState(
            clientOpId = clientOpId,
            state = "FAILED",
            error = error,
            attemptCount = attemptCount
        )
    }
}
