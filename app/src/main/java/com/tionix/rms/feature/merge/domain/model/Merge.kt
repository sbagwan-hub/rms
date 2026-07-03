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

enum class MergeStatus {
    PENDING,
    IN_PROGRESS,
    COMPLETED,
    FAILED
}
