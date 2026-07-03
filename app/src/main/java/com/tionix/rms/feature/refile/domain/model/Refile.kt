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

enum class RefileStatus {
    PENDING,
    IN_PROGRESS,
    COMPLETED,
    FAILED
}
