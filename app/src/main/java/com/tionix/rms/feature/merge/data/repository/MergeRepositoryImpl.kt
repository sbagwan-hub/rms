package com.tionix.rms.feature.merge.data.repository

import com.tionix.rms.feature.merge.data.remote.MergeApiService
import com.tionix.rms.feature.merge.data.remote.dto.toDomain
import com.tionix.rms.feature.merge.data.remote.dto.toDto
import com.tionix.rms.feature.merge.domain.model.Box
import com.tionix.rms.feature.merge.domain.model.Merge
import com.tionix.rms.feature.merge.domain.model.MergeSession
import com.tionix.rms.feature.merge.domain.model.StartMergeRequest
import com.tionix.rms.feature.merge.domain.repository.MergeRepository
import javax.inject.Inject

class MergeRepositoryImpl @Inject constructor(
    private val apiService: MergeApiService
) : MergeRepository {

    override suspend fun getAssignedMerges(): Result<List<Merge>> {
        return try {
            val response = apiService.getAssignedMerges()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.map { it.toDomain() })
            } else {
                Result.failure(Exception("Failed to fetch merges"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun startMerge(request: StartMergeRequest): Result<Merge> {
        return try {
            val response = apiService.startMerge(request.toDto())
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.toDomain())
            } else {
                Result.failure(Exception("Failed to start merge"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun completeMerge(mergeId: String): Result<Unit> {
        return try {
            val response = apiService.completeMerge(mergeId)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to complete merge"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun scanBox(barcode: String): Result<Merge?> {
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

    // Session management — handled locally in use-case layer

    override suspend fun startMergeSession(): Result<MergeSession> {
        return Result.failure(UnsupportedOperationException("Session management is local"))
    }

    override suspend fun scanDestinationBox(barcode: String): Result<Box> {
        return Result.failure(UnsupportedOperationException("Session management is local"))
    }

    override suspend fun scanSourceBox(sessionId: String, barcode: String): Result<Box> {
        return Result.failure(UnsupportedOperationException("Session management is local"))
    }

    override suspend fun removeSourceBox(sessionId: String, boxBarcode: String): Result<Unit> {
        return Result.failure(UnsupportedOperationException("Session management is local"))
    }

    override suspend fun submitMerge(sessionId: String): Result<Unit> {
        return Result.failure(UnsupportedOperationException("Use completeMerge with mergeId"))
    }

    override suspend fun syncMergeToQueue(sessionId: String): Result<Unit> {
        return Result.failure(UnsupportedOperationException("Not yet implemented"))
    }
}
