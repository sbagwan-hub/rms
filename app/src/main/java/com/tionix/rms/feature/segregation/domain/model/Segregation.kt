package com.tionix.rms.feature.segregation.domain.model

data class Segregation(
    val id: String = "",
    val segregationCode: String = "",
    val boxBarcode: String = "",
    val boxName: String? = null,
    val oldBoxBarcode: String? = null,
    val newBoxBarcode: String? = null,
    val sourceLocation: String? = null,
    val destinationLocation: String? = null,
    val status: SegregationStatus = SegregationStatus.IN_PROGRESS,
    val reasonCode: String? = null,
    val reason: String? = null,
    val fileCount: Int = 0,
    val filesMoved: Int = 0,
    val assignedTo: String? = null,
    val startedAt: String? = null,
    val completedAt: String? = null,
    val createdAt: String? = null
)

data class SegregationSession(
    val id: String = "",
    val sessionId: String = "",
    val sourceBox: Box = Box("", "", "", ""),
    val targetBox: Box? = null,
    val sourceFiles: List<FileRecord> = emptyList(),
    val movedFiles: List<FileRecord> = emptyList(),
    val status: SessionStatus = SessionStatus.SCANNING_SOURCE,
    val startTime: String = "",
    val endTime: String? = null,
    val totalFiles: Int = 0,
    val movedCount: Int = 0
)

data class Box(
    val id: String = "",
    val barcode: String = "",
    val description: String = "",
    val location: String = ""
)

data class FileRecord(
    val id: String = "",
    val barcode: String = "",
    val title: String = "",
    val boxBarcode: String = ""
)

enum class SegregationStatus {
    PENDING,
    IN_PROGRESS,
    COMPLETED,
    CANCELLED
}

enum class SessionStatus {
    IDLE,
    SCANNING_SOURCE,
    SCANNING_TARGET,
    MOVING_FILES,
    COMPLETED
}
