package com.tionix.rms.feature.segregation.data.repository

import com.tionix.rms.feature.segregation.data.remote.SegregationApiService
import com.tionix.rms.feature.segregation.domain.model.Segregation
import com.tionix.rms.feature.segregation.domain.model.StartSegregationRequest
import com.tionix.rms.feature.segregation.domain.repository.SegregationRepository
import javax.inject.Inject

class SegregationRepositoryImpl @Inject constructor(
    private val apiService: SegregationApiService
) : SegregationRepository {

    override suspend fun getAssignedSegregations(): Result<List<Segregation>> {
        return try {
            val response = apiService.getAssignedSegregations()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.map { it.toDomain() })
            } else {
                Result.failure(Exception("Failed to fetch segregations"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun startSegregation(request: StartSegregationRequest): Result<Segregation> {
        return try {
            val response = apiService.startSegregation(request.toDto())
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.toDomain())
            } else {
                Result.failure(Exception("Failed to start segregation"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun completeSegregation(segregationId: String): Result<Unit> {
        return try {
            val response = apiService.completeSegregation(segregationId)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to complete segregation"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun scanBox(barcode: String): Result<Segregation?> {
        return try {
            val response = apiService.scanBox(barcode)
            if (response.isSuccessful) {
                Result.success(response.body()?.toDomain())
            } else {
                Result.failure(Exception("Failed to scan box"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
