package com.tionix.rms.feature.merge.data.repository

import com.tionix.rms.feature.merge.data.remote.MergeApiService
import com.tionix.rms.feature.merge.domain.model.Merge
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
}
