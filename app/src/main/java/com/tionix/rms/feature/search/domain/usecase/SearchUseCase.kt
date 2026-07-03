package com.tionix.rms.feature.search.domain.usecase

import com.tionix.rms.feature.search.domain.model.SearchResult
import com.tionix.rms.feature.search.domain.repository.SearchRepository
import com.tionix.rms.feature.search.domain.repository.SearchType

class SearchUseCase(private val repository: SearchRepository) {
    suspend operator fun invoke(query: String, type: SearchType): Result<List<SearchResult>> {
        return repository.search(query, type)
    }
}

class SearchByBarcodeUseCase(private val repository: SearchRepository) {
    suspend operator fun invoke(barcode: String): Result<SearchResult?> {
        return repository.searchByBarcode(barcode)
    }
}
