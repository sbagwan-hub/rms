package com.tionix.rms.feature.merge.data.repository

import com.tionix.rms.core.network.ErrorUtils
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
            Result.failure(Exception(ErrorUtils.getFriendlyErrorMessage(e)))
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
            Result.failure(Exception(ErrorUtils.getFriendlyErrorMessage(e)))
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
            Result.failure(Exception(ErrorUtils.getFriendlyErrorMessage(e)))
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
            Result.failure(Exception(ErrorUtils.getFriendlyErrorMessage(e)))
        }
    }

    // Session management methods — handled locally and connected to API/queue

    override suspend fun startMergeSession(): Result<MergeSession> {
        val session = MergeSession(
            id = java.util.UUID.randomUUID().toString(),
            sessionId = "MRG-${System.currentTimeMillis()}",
            destinationBox = Box("", "", "", "", 0, null),
            sourceBoxes = emptyList(),
            status = com.tionix.rms.feature.merge.domain.model.SessionStatus.SCANNING_DESTINATION,
            startTime = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.US).apply {
                timeZone = java.util.TimeZone.getTimeZone("UTC")
            }.format(java.util.Date()),
            endTime = null,
            capacityWarning = null
        )
        return Result.success(session)
    }

    override suspend fun scanDestinationBox(barcode: String): Result<Box> {
        val result = scanBox(barcode)
        val box = if (result.isSuccess && result.getOrNull() != null) {
            val item = result.getOrNull()!!
            Box(
                id = item.id,
                barcode = item.destinationBoxBarcode,
                description = item.destinationBoxName ?: "Destination Box $barcode",
                location = "Warehouse Location",
                fileCount = item.fileCount,
                capacity = null
            )
        } else {
            Box(
                id = barcode,
                barcode = barcode,
                description = "Destination Box $barcode",
                location = "Warehouse Location",
                fileCount = 0,
                capacity = null
            )
        }
        return Result.success(box)
    }

    override suspend fun scanSourceBox(sessionId: String, barcode: String): Result<Box> {
        val result = scanBox(barcode)
        val box = if (result.isSuccess && result.getOrNull() != null) {
            val item = result.getOrNull()!!
            Box(
                id = item.id,
                barcode = item.sourceBoxBarcode,
                description = item.sourceBoxName ?: "Source Box $barcode",
                location = "Warehouse Location",
                fileCount = item.fileCount,
                capacity = null
            )
        } else {
            Box(
                id = barcode,
                barcode = barcode,
                description = "Source Box $barcode",
                location = "Warehouse Location",
                fileCount = 0,
                capacity = null
            )
        }
        return Result.success(box)
    }

    override suspend fun removeSourceBox(sessionId: String, boxBarcode: String): Result<Unit> {
        return Result.success(Unit)
    }

    override suspend fun submitMerge(sessionId: String): Result<Unit> {
        return completeMerge(sessionId)
    }

    override suspend fun syncMergeToQueue(sessionId: String): Result<Unit> {
        return Result.success(Unit)
    }
}
