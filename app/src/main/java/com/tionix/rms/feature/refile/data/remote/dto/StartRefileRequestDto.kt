package com.tionix.rms.feature.refile.data.remote.dto

data class StartRefileRequestDto(
    val fileBarcode: String,
    val newLocation: String,
    val reason: String?
)
