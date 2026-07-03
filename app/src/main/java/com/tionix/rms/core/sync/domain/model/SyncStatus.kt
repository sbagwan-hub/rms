package com.tionix.rms.core.sync.domain.model

data class SyncStatus(
    val isSyncing: Boolean,
    val lastSyncTime: String?,
    val pendingOperations: Int,
    val failedOperations: Int
)

data class SyncOperation(
    val id: String,
    val type: SyncOperationType,
    val endpoint: String,
    val payload: String,
    val status: SyncOperationStatus,
    val retryCount: Int,
    val createdAt: String,
    val errorMessage: String?
)

enum class SyncOperationType {
    CREATE,
    UPDATE,
    DELETE
}

enum class SyncOperationStatus {
    PENDING,
    IN_PROGRESS,
    COMPLETED,
    FAILED
}
