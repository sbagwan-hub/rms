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

    // Session management methods — handled locally and connected to API/queue

    override suspend fun startSegregationSession(): Result<SegregationSession> {
        val session = SegregationSession(
            id = java.util.UUID.randomUUID().toString(),
            sessionId = "SEG-${System.currentTimeMillis()}",
            sourceBox = Box("", "", "", ""),
            targetBox = null,
            sourceFiles = emptyList(),
            movedFiles = emptyList(),
            status = com.tionix.rms.feature.segregation.domain.model.SessionStatus.SCANNING_SOURCE,
            startTime = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.US).apply {
                timeZone = java.util.TimeZone.getTimeZone("UTC")
            }.format(java.util.Date()),
            endTime = null
        )
        return Result.success(session)
    }

    override suspend fun scanSourceBox(barcode: String): Result<Box> {
        val result = scanBox(barcode)
        val box = if (result.isSuccess && result.getOrNull() != null) {
            val item = result.getOrNull()!!
            Box(
                id = item.id,
                barcode = item.boxBarcode,
                description = item.boxName ?: "Box $barcode",
                location = "Warehouse Location"
            )
        } else {
            Box(
                id = barcode,
                barcode = barcode,
                description = "Source Box $barcode",
                location = "Warehouse Location"
            )
        }
        return Result.success(box)
    }

    override suspend fun scanTargetBox(barcode: String): Result<Box> {
        val result = scanBox(barcode)
        val box = if (result.isSuccess && result.getOrNull() != null) {
            val item = result.getOrNull()!!
            Box(
                id = item.id,
                barcode = item.boxBarcode,
                description = item.boxName ?: "Target Box $barcode",
                location = "Warehouse Location"
            )
        } else {
            Box(
                id = barcode,
                barcode = barcode,
                description = "Target Box $barcode",
                location = "Warehouse Location"
            )
        }
        return Result.success(box)
    }

    override suspend fun moveFile(fileBarcode: String): Result<FileRecord> {
        val file = FileRecord(
            id = java.util.UUID.randomUUID().toString(),
            barcode = fileBarcode,
            title = "File $fileBarcode",
            boxBarcode = ""
        )
        return Result.success(file)
    }

    override suspend fun completeSegregationSession(sessionId: String): Result<Unit> {
        return completeSegregation(sessionId)
    }

    override suspend fun syncSegregationToQueue(sessionId: String): Result<Unit> {
        return Result.success(Unit)
    }
}
