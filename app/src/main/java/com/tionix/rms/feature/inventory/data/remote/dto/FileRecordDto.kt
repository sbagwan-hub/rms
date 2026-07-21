package com.tionix.rms.feature.inventory.data.remote.dto

data class FileRecordDto(
    val id: String,
    val title: String,
    val barcode: String,
    val status: String,
    val boxId: String
)
