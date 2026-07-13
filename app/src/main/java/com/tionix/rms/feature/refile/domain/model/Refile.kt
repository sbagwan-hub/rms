package com.tionix.rms.feature.refile.domain.model

data class Refile(
    val id: String,
    val refileCode: String,
    val fileBarcode: String,
    val fileName: String?,
    val currentLocation: String,
    val newLocation: String,
    val status: RefileStatus,
    val reason: String?,
    val assignedTo: String,
    val startedAt: String?,
    val completedAt: String?,
    val createdAt: String
)

data class FileRecord(
    val id: String,
    val barcode: String,
    val title: String,
    val currentBox: Box,
    val currentLocation: Location
)

data class Box(
    val id: String,
    val barcode: String,
    val description: String,
    val location: Location
)

data class Location(
    val id: String,
    val barcode: String,
    val name: String,
    val room: String,
    val rack: String?,
    val shelf: String?,
    val type: LocationType
)

enum class LocationType {
    ROOM,
    RACK,
    SHELF,
    LOCATION
}

data class RefileAction(
    val id: String,
    val fileRecord: FileRecord,
    val sourceBox: Box,
    val destinationBox: Box,
    val status: RefileActionStatus,
    val timestamp: String,
    val overrideReason: String? = null
)

enum class RefileActionStatus {
    PENDING,
    CONFIRMED,
    REJECTED_MISMATCH,
    OVERRIDDEN
}

data class RefileSession(
    val id: String,
    val sessionId: String,
    val startTime: String,
    val endTime: String?,
    val actions: List<RefileAction>,
    val status: SessionStatus
)

enum class SessionStatus {
    ACTIVE,
    COMPLETED,
    CANCELLED
}

enum class RefileStatus {
    PENDING,
    IN_PROGRESS,
    COMPLETED,
    FAILED
}
