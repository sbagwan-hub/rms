package com.tionix.rms.feature.sync.domain.model

data class SyncItem(
    val id: String,
    val actionType: String,
    val data: String, // JSON payload
    val status: SyncStatus,
    val errorMessage: String?,
    val retryCount: Int,
    val createdAt: String,
    val lastAttemptAt: String?,
    val syncedAt: String?
)

enum class SyncStatus {
    PENDING,
    SYNCED,
    FAILED
}

data class PendingSyncQueue(
    val pendingItems: List<SyncItem>,
    val failedItems: List<SyncItem>,
    val pendingCount: Int,
    val failedCount: Int
)
