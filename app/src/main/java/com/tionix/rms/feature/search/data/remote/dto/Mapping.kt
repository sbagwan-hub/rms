package com.tionix.rms.feature.search.data.remote.dto

import com.tionix.rms.feature.search.domain.model.SearchResult

fun SearchResultDto.toDomain(): SearchResult {
    return when (type) {
        "BOX" -> SearchResult.BoxResult(
            id = id,
            barcode = barcode,
            name = name,
            location = location,
            clientId = clientId ?: "",
            clientName = clientName
        )
        "FILE_RECORD" -> SearchResult.FileRecordResult(
            id = id,
            barcode = barcode,
            title = title ?: "",
            boxBarcode = boxBarcode ?: "",
            location = location
        )
        else -> throw IllegalArgumentException("Unknown search result type: $type")
    }
}
