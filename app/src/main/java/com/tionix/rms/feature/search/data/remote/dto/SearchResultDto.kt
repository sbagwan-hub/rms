package com.tionix.rms.feature.search.data.remote.dto

data class SearchResultDto(
    val type: String,
    val id: String,
    val barcode: String,
    val name: String?,
    val title: String?,
    val location: String,
    val clientId: String?,
    val clientName: String?,
    val boxBarcode: String?
)
