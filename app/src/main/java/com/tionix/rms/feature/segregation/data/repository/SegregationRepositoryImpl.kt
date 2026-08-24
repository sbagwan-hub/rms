package com.tionix.rms.feature.segregation.data.repository

import com.tionix.rms.core.network.ErrorUtils
import com.tionix.rms.feature.segregation.data.remote.SegregationApiService
import com.tionix.rms.feature.segregation.data.remote.dto.*
import com.tionix.rms.feature.segregation.domain.model.Box
import com.tionix.rms.feature.segregation.domain.model.FileRecord
import com.tionix.rms.feature.segregation.domain.model.Segregation
import com.tionix.rms.feature.segregation.domain.model.SegregationSession
import com.tionix.rms.feature.segregation.domain.model.SessionStatus
import com.tionix.rms.feature.segregation.domain.model.StartSegregationRequest
import com.tionix.rms.feature.segregation.domain.repository.SegregationRepository
import org.json.JSONObject
import java.util.UUID
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
                val errorMsg = parseErrorMessage(response.errorBody()?.string()) ?: "Failed to fetch segregations"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(Exception(ErrorUtils.getFriendlyErrorMessage(e)))
        }
    }

    override suspend fun getSegregationDetails(id: String): Result<SegregationSession> {
        return try {
            val response = apiService.getSegregationDetails(id)
            if (response.isSuccessful && response.body() != null) {
                val d = response.body()!!
                val session = SegregationSession(
                    id = d.id,
                    sessionId = d.segregationCode.ifBlank { "SG-${d.id.take(8)}" },
                    sourceBox = Box(
                        id = d.oldBoxId,
                        barcode = d.oldBoxBarcode,
                        description = "Old Box ${d.oldBoxBarcode}",
                        location = d.oldBoxLocation
                    ),
                    targetBox = Box(
                        id = d.newBoxId,
                        barcode = d.newBoxBarcode,
                        description = "New Box ${d.newBoxBarcode}",
                        location = d.newBoxLocation
                    ),
                    sourceFiles = emptyList(),
                    movedFiles = d.movedFileBarcodes.map { barcode ->
                        FileRecord(id = barcode, barcode = barcode, title = "File $barcode", boxBarcode = d.newBoxBarcode)
                    },
                    status = SessionStatus.SCANNING_SOURCE,
                    startTime = d.startedAt ?: "",
                    endTime = d.completedAt,
                    totalFiles = d.totalFilesCount,
                    movedCount = d.filesMovedCount
                )
                Result.success(session)
            } else {
                val errorMsg = parseErrorMessage(response.errorBody()?.string()) ?: "Failed to load segregation details"
                Result.failure(Exception(errorMsg))
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
                val errorMsg = parseErrorMessage(response.errorBody()?.string()) ?: "Failed to start segregation"
                Result.failure(Exception(errorMsg))
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
                val errorMsg = parseErrorMessage(response.errorBody()?.string()) ?: "Failed to complete segregation"
                Result.failure(Exception(errorMsg))
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
                val errorMsg = parseErrorMessage(response.errorBody()?.string()) ?: "Failed to scan box"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(Exception(ErrorUtils.getFriendlyErrorMessage(e)))
        }
    }

    override suspend fun startSegregationSession(): Result<SegregationSession> {
        val session = SegregationSession(
            id = UUID.randomUUID().toString(),
            sessionId = "SEG-${System.currentTimeMillis()}",
            sourceBox = Box("", "", "", ""),
            targetBox = null,
            sourceFiles = emptyList(),
            movedFiles = emptyList(),
            status = SessionStatus.SCANNING_SOURCE,
            startTime = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.US).apply {
                timeZone = java.util.TimeZone.getTimeZone("UTC")
            }.format(java.util.Date()),
            endTime = null
        )
        return Result.success(session)
    }

    override suspend fun scanSourceBox(sessionId: String, barcode: String): Result<Box> {
        return try {
            val cleanBarcode = barcode.trim().replace("\r", "").replace("\n", "").uppercase()
            val response = apiService.validateBox(sessionId, ValidateBoxRequestDto(barcode = cleanBarcode, type = "OLD_BOX"))
            if (response.isSuccessful) {
                Result.success(Box(id = cleanBarcode, barcode = cleanBarcode, description = "Source Box $cleanBarcode", location = "Warehouse"))
            } else {
                val errorMsg = parseErrorMessage(response.errorBody()?.string()) ?: "Wrong old box. Please scan the assigned old box."
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(Exception(ErrorUtils.getFriendlyErrorMessage(e)))
        }
    }

    override suspend fun scanTargetBox(sessionId: String, barcode: String): Result<Box> {
        return try {
            val cleanBarcode = barcode.trim().replace("\r", "").replace("\n", "").uppercase()
            val response = apiService.validateBox(sessionId, ValidateBoxRequestDto(barcode = cleanBarcode, type = "NEW_BOX"))
            if (response.isSuccessful) {
                Result.success(Box(id = cleanBarcode, barcode = cleanBarcode, description = "Target Box $cleanBarcode", location = "Warehouse"))
            } else {
                val errorMsg = parseErrorMessage(response.errorBody()?.string()) ?: "Wrong destination box. Please scan the assigned new box."
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(Exception(ErrorUtils.getFriendlyErrorMessage(e)))
        }
    }

    override suspend fun moveFile(sessionId: String, fileBarcode: String): Result<FileRecord> {
        return try {
            val cleanBarcode = fileBarcode.trim().replace("\r", "").replace("\n", "").uppercase()
            val clientEventId = "SEG-MOVE-$sessionId-$cleanBarcode-${System.currentTimeMillis()}"
            val response = apiService.moveFile(sessionId, MoveFileRequestDto(fileBarcode = cleanBarcode, clientEventId = clientEventId))
            if (response.isSuccessful) {
                val file = FileRecord(
                    id = cleanBarcode,
                    barcode = cleanBarcode,
                    title = "File $cleanBarcode",
                    boxBarcode = ""
                )
                Result.success(file)
            } else {
                val errorMsg = parseErrorMessage(response.errorBody()?.string()) ?: "Failed to move file $cleanBarcode"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(Exception(ErrorUtils.getFriendlyErrorMessage(e)))
        }
    }

    override suspend fun completeSegregationSession(sessionId: String): Result<Unit> {
        return completeSegregation(sessionId)
    }

    override suspend fun syncSegregationToQueue(sessionId: String): Result<Unit> {
        return Result.success(Unit)
    }

    private fun parseErrorMessage(errorBody: String?): String? {
        if (errorBody.isNullOrBlank()) return null
        return try {
            val json = JSONObject(errorBody)
            when {
                json.has("message") -> json.getString("message")
                json.has("error") -> json.getString("error")
                else -> null
            }
        } catch (_: Exception) {
            null
        }
    }
}
