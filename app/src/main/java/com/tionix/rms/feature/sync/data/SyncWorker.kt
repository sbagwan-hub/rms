package com.tionix.rms.feature.sync.data

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.tionix.rms.feature.sync.domain.model.SyncStatus
import com.tionix.rms.feature.sync.domain.repository.SyncRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlin.math.pow
import kotlinx.coroutines.delay
import javax.inject.Inject

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val syncRepository: SyncRepository
) : CoroutineWorker(context, params) {

    companion object {
        const val WORK_NAME = "sync_worker"
    }

    override suspend fun doWork(): Result {
        return try {
            val queue = syncRepository.getPendingSyncQueue()
            if (queue.isFailure) {
                return Result.failure()
            }

            val pendingItems = queue.getOrNull()?.pendingItems ?: emptyList()
            if (pendingItems.isEmpty()) {
                return Result.success()
            }

            // Process in FIFO order
            for (item in pendingItems) {
                try {
                    // TODO: BACKEND ENDPOINT PENDING - Send item to server
                    // Simulate network call with delay
                    delay(1000)
                    
                    syncRepository.markItemSynced(item.id)
                } catch (e: Exception) {
                    syncRepository.markItemFailed(item.id, e.message ?: "Unknown error")
                    
                    // Exponential backoff calculation
                    val retryCount = item.retryCount + 1
                    if (retryCount > 5) {
                        // Max retries reached, stop processing
                        return Result.retry()
                    }
                    
                    val backoffDelay = (2.0.pow(retryCount) * 1000).toLong()
                    delay(backoffDelay)
                }
            }

            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }
}
