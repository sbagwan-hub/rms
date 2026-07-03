package com.tionix.rms.feature.merge.domain.model

data class StartMergeRequest(
    val sourceBoxBarcode: String,
    val destinationBoxBarcode: String,
    val reason: String?
)
