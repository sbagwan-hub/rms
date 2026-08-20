package com.tionix.rms.feature.history.domain.model

data class PendingOperationItem(
    val clientOpId: String,
    val type: String,
    val createdAt: Long,
    val state: String,
    val lastError: String?
)

data class SyncedOperationItem(
    val id: String,
    val type: String,
    val status: String,
    val performedAt: String,
    val summary: String,
    val reasonCode: String?,
    val boxId: String? = null,
    val boxBarcode: String? = null,
    val fileId: String? = null,
    val fileBarcode: String? = null
)
