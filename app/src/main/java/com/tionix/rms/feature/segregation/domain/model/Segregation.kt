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

enum class SegregationStatus {
    PENDING,
    IN_PROGRESS,
    COMPLETED,
    FAILED
}
