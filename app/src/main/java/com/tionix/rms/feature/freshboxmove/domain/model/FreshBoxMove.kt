package com.tionix.rms.feature.freshboxmove.domain.model

data class FreshBoxMove(
    val id: String,
    val boxBarcode: String,
    val boxName: String?,
    val sourceLocation: String,
    val destinationLocation: String,
    val status: MoveStatus,
    val assignedTo: String,
    val startedAt: String?,
    val completedAt: String?,
    val createdAt: String
)

enum class MoveStatus {
    PENDING,
    IN_PROGRESS,
    COMPLETED,
    FAILED
}
