package com.tionix.rms.feature.search.domain.model

sealed class SearchResult {
    data class BoxResult(
        val id: String,
        val barcode: String,
        val name: String?,
        val location: String,
        val clientId: String,
        val clientName: String?
    ) : SearchResult()
    
    data class FileRecordResult(
        val id: String,
        val barcode: String,
        val title: String,
        val boxBarcode: String,
        val location: String
    ) : SearchResult()
}
