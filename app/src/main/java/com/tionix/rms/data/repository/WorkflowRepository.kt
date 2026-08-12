package com.tionix.rms.data.repository

import com.google.gson.Gson
import com.tionix.rms.core.sync.data.local.PendingOperationDao
import com.tionix.rms.core.sync.data.local.PendingOperationEntity
import com.tionix.rms.feature.sync.data.SyncScheduler
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import retrofit2.HttpException

sealed class SubmitOutcome<out T> {
    data class Synced<T>(val value: T) : SubmitOutcome<T>()
    data class Queued(val clientOpId: String) : SubmitOutcome<Nothing>()
}

@Singleton
class WorkflowRepository @Inject constructor(
    private val pendingOperationDao: PendingOperationDao,
    private val syncScheduler: SyncScheduler,
    private val gson: Gson
) {
    suspend fun <T> submitOrEnqueue(
        clientOpId: String,
        type: String,
        payload: Map<String, Any?>,
        onlineCall: suspend () -> T
    ): Result<SubmitOutcome<T>> {
        return try {
            val value = onlineCall()
            Result.success(SubmitOutcome.Synced(value))
        } catch (error: HttpException) {
            if (error.code() in 400..499) {
                Result.failure(Exception(parseHttpError(error)))
            } else {
                enqueue(clientOpId, type, payload)
                Result.success(SubmitOutcome.Queued(clientOpId))
            }
        } catch (error: IOException) {
            enqueue(clientOpId, type, payload)
            Result.success(SubmitOutcome.Queued(clientOpId))
        } catch (error: Exception) {
            val cause = error.cause
            if (cause is IOException) {
                enqueue(clientOpId, type, payload)
                Result.success(SubmitOutcome.Queued(clientOpId))
            } else {
                Result.failure(error)
            }
        }
    }

    suspend fun enqueue(
        clientOpId: String,
        type: String,
        payload: Map<String, Any?>
    ): Result<SubmitOutcome<Nothing>> {
        val payloadWithId = payload.toMutableMap().apply {
            put("clientOpId", clientOpId)
        }
        pendingOperationDao.insert(
            PendingOperationEntity(
                clientOpId = clientOpId,
                type = type,
                payloadJson = gson.toJson(payloadWithId),
                createdAt = System.currentTimeMillis(),
                attemptCount = 0,
                lastError = null,
                state = "QUEUED"
            )
        )
        syncScheduler.scheduleImmediateSync()
        return Result.success(SubmitOutcome.Queued(clientOpId))
    }

    private fun parseHttpError(error: HttpException): String {
        return error.response()?.errorBody()?.string()?.takeIf { it.isNotBlank() }
            ?: "Request failed (${error.code()})"
    }
}
