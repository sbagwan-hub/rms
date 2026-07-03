package com.tionix.rms.feature.segregation.domain.model

data class StartSegregationRequest(
    val boxBarcode: String,
    val reasonCode: String,
    val reason: String?
)
