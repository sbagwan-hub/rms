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

enum class TransferType {
    BOX_TO_LOCATION,
    BOX_TO_WAREHOUSE
}

data class TransferItem(
    val id: String,
    val barcode: String,
    val name: String,
    val currentLocation: String,
    val itemType: ItemType
)

enum class ItemType {
    BOX,
    FILE
}

data class TransferSession(
    val id: String,
    val sessionId: String,
    val transferType: TransferType,
    val sourceItems: List<TransferItem>,
    val destination: String,
    val status: SessionStatus,
    val startTime: String,
    val endTime: String?
)

enum class SessionStatus {
    SELECTING_TYPE,
    SCANNING_SOURCE,
    SELECTING_DESTINATION,
    REVIEWING,
    SUBMITTED,
    QUEUED_OFFLINE
}

enum class TransferStatus {
    PENDING_ACCEPTANCE,
    ACCEPTED,
    COMPLETED,
    REJECTED
}
