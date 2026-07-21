package com.tionix.rms.feature.inventory.data.remote.dto

data class InventoryVerificationDto(
    val id: String,
    val operatorId: String,
    val boxId: String,
    val startedAt: String,
    val endedAt: String?,
    val missingFileCount: Int,
    val unexpectedFileCount: Int,
    val box: BoxDetailsDto,
    val operator: OperatorDetailsDto,
    val scans: List<InventoryVerificationScanDto>? = emptyList()
)

data class BoxDetailsDto(
    val id: String,
    val barcode: String,
    val description: String?
)

data class OperatorDetailsDto(
    val id: String,
    val fullName: String,
    val email: String
)
