package com.tionix.rms.feature.merge.data.remote.dto

data class StartMergeRequestDto(
    val sourceBoxBarcode: String,
    val destinationBoxBarcode: String,
    val reason: String?
)
