package com.tionix.rms.feature.segregation.data.repository

import com.tionix.rms.core.network.ErrorUtils
import com.tionix.rms.feature.segregation.data.remote.SegregationApiService
import com.tionix.rms.feature.segregation.data.remote.dto.toDomain
import com.tionix.rms.feature.segregation.data.remote.dto.toDto
import com.tionix.rms.feature.segregation.domain.model.Box
import com.tionix.rms.feature.segregation.domain.model.FileRecord
import com.tionix.rms.feature.segregation.domain.model.Segregation
import com.tionix.rms.feature.segregation.domain.model.SegregationSession
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
            Result.failure(Exception(ErrorUtils.getFriendlyErrorMessage(e)))
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
            Result.failure(Exception(ErrorUtils.getFriendlyErrorMessage(e)))
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
            Result.failure(Exception(ErrorUtils.getFriendlyErrorMessage(e)))
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
            Result.failure(Exception(ErrorUtils.getFriendlyErrorMessage(e)))
        }
    }

    // Session management methods — handled locally in use-case layer

    override suspend fun startSegregationSession(): Result<SegregationSession> {
        return Result.failure(UnsupportedOperationException("Session management is local"))
    }

    override suspend fun scanSourceBox(barcode: String): Result<Box> {
        return Result.failure(UnsupportedOperationException("Session management is local"))
    }

    override suspend fun scanTargetBox(barcode: String): Result<Box> {
        return Result.failure(UnsupportedOperationException("Session management is local"))
    }

    override suspend fun moveFile(fileBarcode: String): Result<FileRecord> {
        return Result.failure(UnsupportedOperationException("Session management is local"))
    }

    override suspend fun completeSegregationSession(sessionId: String): Result<Unit> {
        return Result.failure(UnsupportedOperationException("Use completeSegregation with segregationId"))
    }

    override suspend fun syncSegregationToQueue(sessionId: String): Result<Unit> {
        return Result.failure(UnsupportedOperationException("Not yet implemented"))
    }
}
