package com.tionix.rms.feature.filesearch.domain.repository

import com.tionix.rms.feature.filesearch.domain.model.FileDetail
import com.tionix.rms.feature.search.domain.model.SearchResult

interface FileSearchRepository {
    suspend fun searchFiles(query: String): Result<List<SearchResult>>
    suspend fun searchFileByBarcode(barcode: String): Result<SearchResult?>
    suspend fun getFileDetail(fileId: String): Result<FileDetail>
}
