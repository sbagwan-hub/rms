package com.tionix.rms.feature.merge.domain.model

data class Merge(
    val id: String,
    val mergeCode: String,
    val sourceBoxBarcode: String,
    val sourceBoxName: String?,
    val destinationBoxBarcode: String,
    val destinationBoxName: String?,
    val status: MergeStatus,
    val reason: String?,
    val fileCount: Int,
    val assignedTo: String,
    val startedAt: String?,
    val completedAt: String?,
    val createdAt: String
)

data class MergeSession(
    val id: String,
    val sessionId: String,
    val destinationBox: Box,
    val sourceBoxes: List<Box>,
    val status: SessionStatus,
    val startTime: String,
    val endTime: String?,
    val capacityWarning: String?
)

data class Box(
    val id: String,
    val barcode: String,
    val description: String,
    val location: String,
    val fileCount: Int,
    val capacity: Int?
)

enum class SessionStatus {
    SCANNING_DESTINATION,
    SCANNING_SOURCES,
    CONFIRMING,
    COMPLETED
}

enum class MergeStatus {
    PENDING,
    IN_PROGRESS,
    COMPLETED,
    FAILED
}
