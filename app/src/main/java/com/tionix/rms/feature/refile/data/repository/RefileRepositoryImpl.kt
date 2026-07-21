package com.tionix.rms.feature.refile.data.repository

import com.tionix.rms.core.network.ErrorUtils
import com.tionix.rms.feature.refile.data.remote.RefileApiService
import com.tionix.rms.feature.refile.data.remote.dto.toDomain
import com.tionix.rms.feature.refile.data.remote.dto.toDto
import com.tionix.rms.feature.refile.domain.model.FileRecord
import com.tionix.rms.feature.refile.domain.model.Refile
import com.tionix.rms.feature.refile.domain.model.RefileAction
import com.tionix.rms.feature.refile.domain.model.RefileSession
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
            Result.failure(Exception(ErrorUtils.getFriendlyErrorMessage(e)))
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
            Result.failure(Exception(ErrorUtils.getFriendlyErrorMessage(e)))
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
            Result.failure(Exception(ErrorUtils.getFriendlyErrorMessage(e)))
        }
    }

    // scanFile: interface expects Result<FileRecord>; file-record scan is handled via the
    // use-case layer locally. Return failure to satisfy the contract until the endpoint exists.
    override suspend fun scanFile(barcode: String): Result<FileRecord> {
        return Result.failure(UnsupportedOperationException("Scan handled by use-case layer"))
    }

    // Refile workflow methods — use-case layer manages local session state
    override suspend fun getHomeLocation(fileBarcode: String): Result<FileRecord> {
        return Result.failure(UnsupportedOperationException("Not yet implemented"))
    }

    override suspend fun confirmRefile(
        fileBarcode: String,
        destinationBoxBarcode: String
    ): Result<RefileAction> {
        return Result.failure(UnsupportedOperationException("Not yet implemented"))
    }

    override suspend fun overrideMismatch(
        fileBarcode: String,
        destinationBoxBarcode: String,
        reason: String
    ): Result<RefileAction> {
        return Result.failure(UnsupportedOperationException("Not yet implemented"))
    }

    override suspend fun startSession(): Result<RefileSession> {
        return Result.failure(UnsupportedOperationException("Session management is local"))
    }

    override suspend fun endSession(sessionId: String): Result<Unit> {
        return Result.failure(UnsupportedOperationException("Session management is local"))
    }

    override suspend fun undoLastAction(sessionId: String): Result<Unit> {
        return Result.failure(UnsupportedOperationException("Not yet implemented"))
    }

    override suspend fun syncRefileActionToQueue(action: RefileAction): Result<Unit> {
        return Result.failure(UnsupportedOperationException("Not yet implemented"))
    }
}
