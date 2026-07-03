package com.tionix.rms.feature.freshboxmove.domain.model

data class StartMoveRequest(
    val boxBarcode: String,
    val destinationLocation: String
)
