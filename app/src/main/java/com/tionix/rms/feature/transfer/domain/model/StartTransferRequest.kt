package com.tionix.rms.feature.transfer.domain.model

data class StartTransferRequest(
    val boxBarcode: String,
    val destinationLocation: String,
    val reason: String?
)
