package com.tionix.rms.feature.refile.domain.model

data class StartRefileRequest(
    val fileBarcode: String,
    val newLocation: String,
    val reason: String?
)
