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
    val createdAt: String,
    val expectedBoxes: List<Box> = emptyList(),
    val scannedBoxes: List<ScannedBox> = emptyList()
)

data class Box(
    val id: String,
    val barcode: String,
    val description: String,
    val currentLocation: String,
    val expectedLocation: String,
    val status: BoxStatus = BoxStatus.PENDING
)

data class ScannedBox(
    val barcode: String,
    val scanStatus: ScanStatus,
    val timestamp: String
)

enum class VerificationStatus {
    PENDING,
    IN_PROGRESS,
    COMPLETED,
    FAILED
}

enum class BoxStatus {
    PENDING,
    VERIFIED,
    MISSING,
    UNEXPECTED
}

enum class ScanStatus {
    VERIFIED,
    UNEXPECTED,
    DUPLICATE
}
