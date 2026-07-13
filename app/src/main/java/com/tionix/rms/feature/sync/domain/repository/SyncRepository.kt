package com.tionix.rms.feature.sync.domain.repository

import com.tionix.rms.feature.sync.domain.model.PendingSyncQueue
import com.tionix.rms.feature.sync.domain.model.SyncItem
import kotlinx.coroutines.flow.Flow

interface SyncRepository {
    suspend fun getPendingSyncQueue(): Result<PendingSyncQueue>
    fun observePendingSyncQueue(): Flow<PendingSyncQueue>
    suspend fun retrySyncItem(itemId: String): Result<Unit>
    suspend fun retryAllFailedItems(): Result<Unit>
    suspend fun deleteFailedItem(itemId: String): Result<Unit>
    suspend fun deleteAllFailedItems(): Result<Unit>
    suspend fun markItemSynced(itemId: String): Result<Unit>
    suspend fun markItemFailed(itemId: String, errorMessage: String): Result<Unit>
}
