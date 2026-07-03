package com.tionix.rms.feature.transfer.data.remote.dto

data class StartTransferRequestDto(
    val boxBarcode: String,
    val destinationLocation: String,
    val reason: String?
)
