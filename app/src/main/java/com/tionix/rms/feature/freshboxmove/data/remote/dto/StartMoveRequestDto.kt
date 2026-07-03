package com.tionix.rms.feature.freshboxmove.data.remote.dto

data class StartMoveRequestDto(
    val boxBarcode: String,
    val destinationLocation: String
)
