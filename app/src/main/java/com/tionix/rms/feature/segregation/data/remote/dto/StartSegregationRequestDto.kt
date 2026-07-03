package com.tionix.rms.feature.segregation.data.remote.dto

data class StartSegregationRequestDto(
    val boxBarcode: String,
    val reasonCode: String,
    val reason: String?
)
