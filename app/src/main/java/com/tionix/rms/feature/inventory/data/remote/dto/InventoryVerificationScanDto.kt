package com.tionix.rms.feature.inventory.data.remote.dto

data class InventoryVerificationScanDto(
    val id: String,
    val sessionId: String,
    val boxId: String,
    val fileRecordId: String?,
    val clientEventId: String,
    val isExpected: Boolean,
    val isMissingFlag: Boolean,
    val scannedAt: String,
    val fileRecord: FileRecordDetailsDto?
)

data class FileRecordDetailsDto(
    val id: String,
    val title: String,
    val barcode: String,
    val status: String
)
