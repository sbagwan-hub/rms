package com.tionix.rms.feature.filesearch.domain.model

data class FileDetail(
    val id: String,
    val barcode: String,
    val title: String,
    val clientName: String? = null,
    val fileType: String? = null,
    val parentBox: ParentBox,
    val locationChain: List<String>,
    val status: FileStatus,
    val movementHistory: List<MovementEvent>,
    val createdAt: String,
    val updatedAt: String?
)

data class ParentBox(
    val id: String,
    val barcode: String,
    val name: String?,
    val location: String,
    val warehouse: String? = null
)

data class MovementEvent(
    val id: String,
    val eventType: MovementType,
    val fromLocation: String?,
    val toLocation: String?,
    val timestamp: String,
    val performedBy: String,
    val notes: String?
)

enum class MovementType {
    CREATED,
    CHECKED_IN,
    CHECKED_OUT,
    TRANSFERRED,
    REFILED,
    SEGREGATED,
    MERGED
}

enum class FileStatus {
    ACTIVE,
    CHECKED_OUT,
    ARCHIVED,
    LOST
}
