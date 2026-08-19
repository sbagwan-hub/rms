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
        return try {
            val response = apiService.getBoxDetail(boxId)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.toDomain())
            } else {
                Result.failure(Exception("Failed to load box details"))
            }
        } catch (e: Exception) {
            Result.failure(Exception(ErrorUtils.getFriendlyErrorMessage(e)))
        }
    }

    override suspend fun insertFile(boxId: String, fileBarcode: String, title: String?): Result<String> {
        return try {
            val response = apiService.insertFile(
                boxId = boxId,
                request = com.tionix.rms.feature.search.data.remote.InsertFileRequest(
                    fileBarcode = fileBarcode,
                    title = title
                )
            )
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()?.message ?: "File inserted successfully")
            } else {
                val errorBodyStr = response.errorBody()?.string()
                val serverMsg = try {
                    if (!errorBodyStr.isNullOrBlank()) {
                        val json = com.google.gson.JsonParser.parseString(errorBodyStr).asJsonObject
                        if (json.has("error") && json.get("error").isJsonObject) {
                            json.getAsJsonObject("error").get("message")?.asString
                        } else if (json.has("message")) {
                            json.get("message")?.asString
                        } else null
                    } else null
                } catch (ex: Exception) {
                    null
                }
                Result.failure(Exception(serverMsg ?: response.body()?.message ?: "Failed to insert file"))
            }
        } catch (e: Exception) {
            Result.failure(Exception(ErrorUtils.getFriendlyErrorMessage(e)))
        }
    }
}
