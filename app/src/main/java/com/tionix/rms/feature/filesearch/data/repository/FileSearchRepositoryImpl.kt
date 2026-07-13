package com.tionix.rms.feature.filesearch.data.repository

import com.tionix.rms.feature.filesearch.domain.model.FileDetail
import com.tionix.rms.feature.filesearch.domain.model.FileStatus
import com.tionix.rms.feature.filesearch.domain.model.ParentBox
import com.tionix.rms.feature.filesearch.domain.repository.FileSearchRepository
import com.tionix.rms.feature.search.domain.model.SearchResult
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FileSearchRepositoryImpl @Inject constructor() : FileSearchRepository {
    override suspend fun searchFiles(query: String): Result<List<SearchResult>> {
        return Result.success(emptyList())
    }

    override suspend fun searchFileByBarcode(barcode: String): Result<SearchResult?> {
        return Result.success(null)
    }

    override suspend fun getFileDetail(fileId: String): Result<FileDetail> {
        return Result.success(
            FileDetail(
                id = fileId,
                barcode = "FILE-001",
                title = "Mock File",
                parentBox = ParentBox(
                    id = "box_1",
                    barcode = "BOX-001",
                    name = "Central Box",
                    location = "Room A"
                ),
                locationChain = emptyList(),
                status = FileStatus.ACTIVE,
                movementHistory = emptyList(),
                createdAt = "2026-07-13",
                updatedAt = null
            )
        )
    }
}
