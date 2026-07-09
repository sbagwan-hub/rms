package com.tionix.rms.feature.transfer.domain.model

data class Transfer(
    val id: String,
    val transferCode: String,
    val boxBarcode: String,
    val boxName: String?,
    val sourceLocation: String,
    val destinationLocation: String,
    val status: TransferStatus,
    val reason: String?,
    val assignedTo: String,
    val startedAt: String?,
    val completedAt: String?,
    val createdAt: String
)

enum class TransferStatus {
    PENDING_ACCEPTANCE,
    ACCEPTED,
    COMPLETED,
    REJECTED
}
