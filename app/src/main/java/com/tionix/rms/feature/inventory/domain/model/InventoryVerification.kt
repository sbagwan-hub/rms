package com.tionix.rms.feature.inventory.domain.model

data class InventoryVerification(
    val id: String,
    val verificationCode: String,
    val locationId: String,
    val locationName: String,
    val status: VerificationStatus,
    val totalBoxes: Int,
    val verifiedBoxes: Int,
    val discrepancyBoxes: Int,
    val assignedTo: String,
    val startedAt: String?,
    val completedAt: String?,
    val createdAt: String
)

enum class VerificationStatus {
    PENDING,
    IN_PROGRESS,
    COMPLETED,
    FAILED
}
