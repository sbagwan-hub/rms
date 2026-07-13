package com.tionix.rms.feature.search.domain.usecase

import com.tionix.rms.feature.search.domain.model.BoxDetail
import com.tionix.rms.feature.search.domain.model.SearchResult
import com.tionix.rms.feature.search.domain.repository.SearchRepository
import com.tionix.rms.feature.search.domain.repository.SearchType
import javax.inject.Inject

class SearchUseCase @Inject constructor(private val repository: SearchRepository) {
    suspend operator fun invoke(query: String, type: SearchType): Result<List<SearchResult>> {
        // TODO: BACKEND ENDPOINT PENDING
        return repository.search(query, type)
    }
}

class SearchByBarcodeUseCase @Inject constructor(private val repository: SearchRepository) {
    suspend operator fun invoke(barcode: String): Result<SearchResult?> {
        // TODO: BACKEND ENDPOINT PENDING
        return repository.searchByBarcode(barcode)
    }
}

class GetBoxDetailUseCase @Inject constructor(private val repository: SearchRepository) {
    suspend operator fun invoke(boxId: String): Result<BoxDetail> {
        // TODO: BACKEND ENDPOINT PENDING
        return repository.getBoxDetail(boxId)
    }
}
