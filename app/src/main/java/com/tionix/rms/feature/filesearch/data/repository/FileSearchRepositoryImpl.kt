package com.tionix.rms.feature.filesearch.data.repository

import com.tionix.rms.core.network.ErrorUtils
import com.tionix.rms.feature.filesearch.data.remote.dto.toDomain
import com.tionix.rms.feature.filesearch.domain.model.FileDetail
import com.tionix.rms.feature.filesearch.domain.repository.FileSearchRepository
import com.tionix.rms.feature.search.data.remote.SearchApiService
import com.tionix.rms.feature.search.data.remote.dto.toDomain
import com.tionix.rms.feature.search.domain.model.SearchResult
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FileSearchRepositoryImpl @Inject constructor(
    private val apiService: SearchApiService
) : FileSearchRepository {

    override suspend fun searchFiles(query: String): Result<List<SearchResult>> {
        return try {
            val response = apiService.search(query, "FILE")
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.map { it.toDomain() })
            } else {
                Result.failure(Exception("File search failed"))
            }
        } catch (e: Exception) {
            Result.failure(Exception(ErrorUtils.getFriendlyErrorMessage(e)))
        }
    }

    override suspend fun searchFileByBarcode(barcode: String): Result<SearchResult?> {
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

    private fun parseErrorMessage(response: retrofit2.Response<*>): String {
        return try {
            val errorBody = response.errorBody()?.string()
            if (!errorBody.isNullOrEmpty()) {
                val json = org.json.JSONObject(errorBody)
                if (json.has("error")) {
                    val errObj = json.getJSONObject("error")
                    errObj.optString("message", "")
                } else {
                    json.optString("message", "")
                }
            } else ""
        } catch (e: Exception) {
            ""
        }
    }

    override suspend fun getFileDetail(fileId: String): Result<FileDetail> {
        return try {
            val cleanId = fileId.trim().replace("\r", "").replace("\n", "").replace("\t", "")
            val response = apiService.getFileDetail(cleanId)
            if (response.isSuccessful && response.body()?.data != null) {
                Result.success(response.body()!!.data!!.toDomain())
            } else {
                val errorMsg = parseErrorMessage(response)
                Result.failure(Exception(if (errorMsg.isNotBlank()) errorMsg else "File barcode $cleanId was not found in the system."))
            }
        } catch (e: Exception) {
            Result.failure(Exception(ErrorUtils.getFriendlyErrorMessage(e)))
        }
    }
}
