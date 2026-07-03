package com.tionix.rms.feature.inventory.data.remote.dto

data class InventoryVerificationDto(
    val id: String,
    val verificationCode: String,
    val locationId: String,
    val locationName: String,
    val status: String,
    val totalBoxes: Int,
    val verifiedBoxes: Int,
    val discrepancyBoxes: Int,
    val assignedTo: String,
    val startedAt: String?,
    val completedAt: String?,
    val createdAt: String
)
