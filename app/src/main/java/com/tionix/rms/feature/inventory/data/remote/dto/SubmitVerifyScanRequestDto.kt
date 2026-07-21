package com.tionix.rms.feature.inventory.data.remote.dto

data class SubmitVerifyScanRequestDto(
    val fileBarcode: String,
    val clientEventId: String,
    val scannedAt: String
)
