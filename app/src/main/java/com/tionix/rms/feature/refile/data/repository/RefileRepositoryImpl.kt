package com.tionix.rms.feature.refile.data.repository

import com.tionix.rms.feature.refile.data.remote.RefileApiService
import com.tionix.rms.feature.refile.domain.model.Refile
import com.tionix.rms.feature.refile.domain.model.StartRefileRequest
import com.tionix.rms.feature.refile.domain.repository.RefileRepository
import javax.inject.Inject

class RefileRepositoryImpl @Inject constructor(
    private val apiService: RefileApiService
) : RefileRepository {

    override suspend fun getAssignedRefiles(): Result<List<Refile>> {
        return try {
            val response = apiService.getAssignedRefiles()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.map { it.toDomain() })
            } else {
                Result.failure(Exception("Failed to fetch refiles"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun startRefile(request: StartRefileRequest): Result<Refile> {
        return try {
            val response = apiService.startRefile(request.toDto())
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.toDomain())
            } else {
                Result.failure(Exception("Failed to start refile"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun completeRefile(refileId: String): Result<Unit> {
        return try {
            val response = apiService.completeRefile(refileId)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to complete refile"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun scanFile(barcode: String): Result<Refile?> {
        return try {
            val response = apiService.scanFile(barcode)
            if (response.isSuccessful) {
                Result.success(response.body()?.toDomain())
            } else {
                Result.failure(Exception("Failed to scan file"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
