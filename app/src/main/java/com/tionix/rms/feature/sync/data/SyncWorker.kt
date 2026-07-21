package com.tionix.rms.feature.sync.data

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.gson.Gson
import com.tionix.rms.feature.freshboxmove.data.local.FreshBoxDao
import com.tionix.rms.feature.freshboxmove.data.remote.FreshBoxApi
import com.tionix.rms.feature.freshboxmove.data.remote.StartSessionRequestDto
import com.tionix.rms.feature.freshboxmove.data.remote.SubmitScanRequestDto
import com.tionix.rms.feature.sync.domain.model.SyncItem
import com.tionix.rms.feature.sync.domain.repository.SyncRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import retrofit2.Response

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val syncRepository: SyncRepository,
    private val freshBoxApi: FreshBoxApi,
    private val freshBoxDao: FreshBoxDao,
) : CoroutineWorker(context, params) {

    companion object {
        const val WORK_NAME = "sync_worker"
        private const val MAX_RETRIES = 5
    }

    private val gson = Gson()

    override suspend fun doWork(): Result {
        val queue = syncRepository.getPendingSyncQueue()
        if (queue.isFailure) {
            return Result.failure()
        }

        val pendingItems = queue.getOrNull()?.pendingItems ?: emptyList()
        if (pendingItems.isEmpty()) {
            return Result.success()
        }

        var anyRetryable = false

        // FIFO order: a session's START op is always queued (and therefore
        // processed) before its SUBMIT/END ops, so serverSessionId is
        // resolved by the time we reach them in the same pass.
        for (item in pendingItems) {
            val outcome = dispatch(item)
            when (outcome) {
                is DispatchOutcome.Success -> syncRepository.markItemSynced(item.id)
                is DispatchOutcome.Rejected -> syncRepository.markItemFailed(item.id, outcome.message)
                is DispatchOutcome.Retryable -> {
                    anyRetryable = true
                    if (item.retryCount + 1 > MAX_RETRIES) {
                        syncRepository.markItemFailed(item.id, outcome.message)
                    }
                    // else: leave PENDING: it will be retried on the worker's
                    // next scheduled run (WorkManager's own backoff policy
                    // governs run spacing — no in-worker delay/sleep here).
                }
                is DispatchOutcome.NotYetResolvable -> {
                    // Parent session hasn't synced yet (shouldn't normally
                    // happen given FIFO ordering) — leave PENDING, no penalty.
                }
            }
        }

        return if (anyRetryable) Result.retry() else Result.success()
    }

    private sealed class DispatchOutcome {
        object Success : DispatchOutcome()
        data class Rejected(val message: String) : DispatchOutcome()
        data class Retryable(val message: String) : DispatchOutcome()
        object NotYetResolvable : DispatchOutcome()
    }

    private suspend fun dispatch(item: SyncItem): DispatchOutcome {
        return try {
            when (item.actionType) {
                "START_FRESH_BOX_SESSION" -> startFreshBoxSession(item)
                "SUBMIT_FRESH_BOX_SCAN" -> submitFreshBoxScan(item)
                "END_FRESH_BOX_SESSION" -> endFreshBoxSession(item)
                else -> DispatchOutcome.Rejected("Unknown sync operation type: ${item.actionType}")
            }
        } catch (e: Exception) {
            DispatchOutcome.Retryable(e.message ?: "Network error")
        }
    }

    private suspend fun startFreshBoxSession(item: SyncItem): DispatchOutcome {
        val payload = gson.fromJson(item.data, StartSessionPayload::class.java)
        val response = freshBoxApi.startSession(StartSessionRequestDto(payload.deviceId))
        return handleResponse(response) { body ->
            freshBoxDao.updateServerSessionId(payload.clientSessionId, body.id)
        }
    }

    private suspend fun submitFreshBoxScan(item: SyncItem): DispatchOutcome {
        val payload = gson.fromJson(item.data, SubmitScanPayload::class.java)
        val session = freshBoxDao.getSession(payload.clientSessionId)
        val serverSessionId = session?.serverSessionId
            ?: return DispatchOutcome.NotYetResolvable

        val response = freshBoxApi.submitScan(
            sessionId = serverSessionId,
            request = SubmitScanRequestDto(
                locationBarcode = payload.locationBarcode,
                boxBarcode = payload.boxBarcode,
                clientEventId = payload.clientEventId,
                gpsLat = payload.gpsLat,
                gpsLng = payload.gpsLng,
                scannedAt = payload.scannedAt,
            )
        )
        return handleResponse(response) {
            freshBoxDao.markScanAsSynced(payload.clientEventId)
        }
    }

    private suspend fun endFreshBoxSession(item: SyncItem): DispatchOutcome {
        val payload = gson.fromJson(item.data, EndSessionPayload::class.java)
        val session = freshBoxDao.getSession(payload.clientSessionId)
        val serverSessionId = session?.serverSessionId
            ?: return DispatchOutcome.NotYetResolvable

        val response = freshBoxApi.endSession(serverSessionId)
        return handleResponse(response) {}
    }

    /**
     * Maps an HTTP response to a dispatch outcome: 2xx = success (with the
     * body available for local-state updates), 4xx = a definitive rejection
     * (bad data / business-rule violation, e.g. LOCATION_OCCUPIED) that must
     * not be retried, 5xx = transient/server trouble, retried with backoff.
     */
    private inline fun <T> handleResponse(response: Response<T>, onSuccess: (T) -> Unit): DispatchOutcome {
        if (response.isSuccessful) {
            response.body()?.let(onSuccess)
            return DispatchOutcome.Success
        }
        val errorMessage = response.errorBody()?.string() ?: response.message()
        return if (response.code() in 400..499) {
            DispatchOutcome.Rejected(errorMessage)
        } else {
            DispatchOutcome.Retryable(errorMessage)
        }
    }

    private data class StartSessionPayload(val clientSessionId: String, val deviceId: String?)
    private data class SubmitScanPayload(
        val clientEventId: String,
        val clientSessionId: String,
        val boxBarcode: String,
        val locationBarcode: String,
        val gpsLat: Double?,
        val gpsLng: Double?,
        val scannedAt: String,
    )
    private data class EndSessionPayload(val clientSessionId: String)
}
