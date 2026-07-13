package com.tionix.rms.feature.segregation.domain.model

data class Segregation(
    val id: String,
    val segregationCode: String,
    val boxBarcode: String,
    val boxName: String?,
    val status: SegregationStatus,
    val reasonCode: String?,
    val reason: String?,
    val fileCount: Int,
    val assignedTo: String,
    val startedAt: String?,
    val completedAt: String?,
    val createdAt: String
)

data class SegregationSession(
    val id: String,
    val sessionId: String,
    val sourceBox: Box,
    val targetBox: Box?,
    val sourceFiles: List<FileRecord>,
    val movedFiles: List<FileRecord>,
    val status: SessionStatus,
    val startTime: String,
    val endTime: String?
)

data class Box(
    val id: String,
    val barcode: String,
    val description: String,
    val location: String
)

data class FileRecord(
    val id: String,
    val barcode: String,
    val title: String,
    val boxBarcode: String
)

enum class SessionStatus {
    SCANNING_SOURCE,
    SCANNING_TARGET,
    MOVING_FILES,
    COMPLETED
}

enum class SegregationStatus {
    PENDING,
    IN_PROGRESS,
    COMPLETED,
    FAILED
}
