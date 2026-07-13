package com.tionix.rms.feature.filesearch.domain.usecase

import com.tionix.rms.feature.filesearch.domain.model.FileDetail
import com.tionix.rms.feature.filesearch.domain.repository.FileSearchRepository
import com.tionix.rms.feature.search.domain.model.SearchResult
import javax.inject.Inject

class SearchFilesUseCase @Inject constructor(
    private val repository: FileSearchRepository
) {
    suspend operator fun invoke(query: String): Result<List<SearchResult>> {
        // TODO: BACKEND ENDPOINT PENDING
        return repository.searchFiles(query)
    }
}

class SearchFileByBarcodeUseCase @Inject constructor(
    private val repository: FileSearchRepository
) {
    suspend operator fun invoke(barcode: String): Result<SearchResult?> {
        // TODO: BACKEND ENDPOINT PENDING
        return repository.searchFileByBarcode(barcode)
    }
}

class GetFileDetailUseCase @Inject constructor(
    private val repository: FileSearchRepository
) {
    suspend operator fun invoke(fileId: String): Result<FileDetail> {
        // TODO: BACKEND ENDPOINT PENDING
        return repository.getFileDetail(fileId)
    }
}
