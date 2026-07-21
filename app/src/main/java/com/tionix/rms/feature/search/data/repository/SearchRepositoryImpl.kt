package com.tionix.rms.feature.search.data.repository

import com.tionix.rms.core.network.ErrorUtils
import com.tionix.rms.feature.search.data.remote.SearchApiService
import com.tionix.rms.feature.search.data.remote.dto.toDomain
import com.tionix.rms.feature.search.domain.model.BoxDetail
import com.tionix.rms.feature.search.domain.model.SearchResult
import com.tionix.rms.feature.search.domain.repository.SearchRepository
import com.tionix.rms.feature.search.domain.repository.SearchType
import javax.inject.Inject

class SearchRepositoryImpl @Inject constructor(
    private val apiService: SearchApiService
) : SearchRepository {

    override suspend fun search(query: String, type: SearchType): Result<List<SearchResult>> {
        return try {
            val response = apiService.search(query, type.name)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.map { it.toDomain() })
            } else {
                Result.failure(Exception("Search failed"))
            }
        } catch (e: Exception) {
            Result.failure(Exception(ErrorUtils.getFriendlyErrorMessage(e)))
        }
    }

    override suspend fun searchByBarcode(barcode: String): Result<SearchResult?> {
        return try {
            val response = apiService.searchByBarcode(barcode)
            if (response.isSuccessful) {
                Result.success(response.body()?.toDomain())
            } else {
                Result.failure(Exception("Barcode search failed"))
            }
        } catch (e: Exception) {
            Result.failure(Exception(ErrorUtils.getFriendlyErrorMessage(e)))
        }
    }

    override suspend fun getBoxDetail(boxId: String): Result<BoxDetail> {
        // TODO: Add getBoxDetail endpoint to SearchApiService when backend is ready
        return Result.failure(UnsupportedOperationException("getBoxDetail endpoint not yet available"))
    }
}
