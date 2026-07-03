package com.tionix.rms.feature.search.domain.repository

import com.tionix.rms.feature.search.domain.model.SearchResult

interface SearchRepository {
    suspend fun search(query: String, type: SearchType): Result<List<SearchResult>>
    suspend fun searchByBarcode(barcode: String): Result<SearchResult?>
}

enum class SearchType {
    ALL,
    BOX,
    FILE_RECORD
}
