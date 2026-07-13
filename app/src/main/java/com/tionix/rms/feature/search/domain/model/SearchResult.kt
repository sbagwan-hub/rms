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

data class BoxDetail(
    val id: String,
    val barcode: String,
    val name: String?,
    val location: String,
    val status: BoxStatus,
    val fileCount: Int,
    val lastActivity: String?,
    val contents: List<FileRecord>,
    val clientId: String,
    val clientName: String?
)

data class FileRecord(
    val id: String,
    val barcode: String,
    val title: String,
    val boxBarcode: String
)

enum class BoxStatus {
    ACTIVE,
    IN_TRANSIT,
    LOCKED,
    ARCHIVED
}
