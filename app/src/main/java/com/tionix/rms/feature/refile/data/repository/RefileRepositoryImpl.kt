package com.tionix.rms.feature.refile.data.repository

import com.tionix.rms.core.network.ErrorUtils
import com.tionix.rms.feature.refile.data.remote.RefileApiService
import com.tionix.rms.feature.refile.data.remote.dto.toDomain
import com.tionix.rms.feature.refile.data.remote.dto.toDto
import com.tionix.rms.feature.refile.domain.model.*
import com.tionix.rms.feature.refile.domain.repository.RefileRepository
import javax.inject.Inject

class RefileRepositoryImpl @Inject constructor(
    private val apiService: RefileApiService,
    private val searchApiService: com.tionix.rms.feature.search.data.remote.SearchApiService
) : RefileRepository {

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

    private fun cleanBarcodeString(b: String): String =
        b.trim().replace("\r", "").replace("\n", "").replace("\t", "")

    override suspend fun scanFile(barcode: String): Result<FileRecord> {
        return try {
            val cleanBarcode = cleanBarcodeString(barcode)
            val response = searchApiService.getFileDetail(cleanBarcode)
            if (response.isSuccessful && response.body()?.data != null) {
                val detail = response.body()!!.data!!
                val location = Location(
                    id = if (detail.parentBox.id.isNotBlank()) detail.parentBox.id else detail.id,
                    barcode = detail.parentBox.location,
                    name = detail.parentBox.location,
                    room = "",
                    rack = "",
                    shelf = "",
                    type = LocationType.LOCATION
                )
                val box = Box(
                    id = detail.parentBox.id,
                    barcode = detail.parentBox.barcode,
                    description = detail.parentBox.name ?: "Box ${detail.parentBox.barcode}",
                    location = location
                )
                val fileRecord = FileRecord(
                    id = detail.id,
                    barcode = detail.barcode,
                    title = detail.title,
                    currentBox = box,
                    currentLocation = location
                )
                Result.success(fileRecord)
            } else {
                val errorMsg = parseErrorMessage(response)
                Result.failure(Exception(if (errorMsg.isNotBlank()) errorMsg else "File barcode $cleanBarcode is not registered. Please register the file before refiling."))
            }
        } catch (e: Exception) {
            Result.failure(Exception(ErrorUtils.getFriendlyErrorMessage(e)))
        }
    }

    // Refile workflow methods — use-case layer manages local session state
    override suspend fun getHomeLocation(fileBarcode: String): Result<FileRecord> {
        return scanFile(fileBarcode)
    }

    override suspend fun confirmRefile(
        fileBarcode: String,
        destinationBoxBarcode: String
    ): Result<RefileAction> {
        return try {
            val cleanFile = cleanBarcodeString(fileBarcode)
            val cleanDest = cleanBarcodeString(destinationBoxBarcode)
            val response = searchApiService.refileFile(
                com.tionix.rms.feature.search.data.remote.RefileRequest(
                    fileBarcode = cleanFile,
                    targetBoxBarcode = cleanDest
                )
            )
            if (response.isSuccessful && response.body()?.success == true) {
                val data = response.body()?.data
                val location = Location(
                    id = data?.newBoxId ?: data?.targetBoxId ?: cleanDest,
                    barcode = data?.newBoxBarcode ?: data?.targetBoxBarcode ?: cleanDest,
                    name = data?.newLocation ?: cleanDest,
                    room = "",
                    rack = "",
                    shelf = "",
                    type = LocationType.LOCATION
                )
                val srcBox = Box(
                    id = data?.previousBoxId ?: data?.sourceBoxId ?: "",
                    barcode = data?.previousBoxBarcode ?: data?.sourceBoxBarcode ?: "Unassigned",
                    description = "Previous Box",
                    location = location
                )
                val dstBox = Box(
                    id = data?.newBoxId ?: data?.targetBoxId ?: cleanDest,
                    barcode = data?.newBoxBarcode ?: data?.targetBoxBarcode ?: cleanDest,
                    description = "New Box ${data?.newBoxBarcode ?: cleanDest}",
                    location = location
                )
                val file = FileRecord(
                    id = data?.fileId ?: cleanFile,
                    barcode = data?.fileBarcode ?: cleanFile,
                    title = "File ${data?.fileBarcode ?: cleanFile}",
                    currentBox = dstBox,
                    currentLocation = location
                )
                val action = RefileAction(
                    id = data?.fileId ?: cleanFile,
                    fileRecord = file,
                    sourceBox = srcBox,
                    destinationBox = dstBox,
                    status = RefileActionStatus.CONFIRMED,
                    timestamp = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.US).apply {
                        timeZone = java.util.TimeZone.getTimeZone("UTC")
                    }.format(java.util.Date()),
                    overrideReason = null
                )
                Result.success(action)
            } else {
                val errorMsg = parseErrorMessage(response)
                Result.failure(Exception(if (errorMsg.isNotBlank()) errorMsg else "Refile failed"))
            }
        } catch (e: Exception) {
            Result.failure(Exception(ErrorUtils.getFriendlyErrorMessage(e)))
        }
    }

    override suspend fun overrideMismatch(
        fileBarcode: String,
        destinationBoxBarcode: String,
        reason: String
    ): Result<RefileAction> {
        return try {
            val startRes = apiService.startRefile(
                com.tionix.rms.feature.refile.data.remote.dto.StartRefileRequestDto(
                    fileBarcode = fileBarcode,
                    newLocation = destinationBoxBarcode,
                    reason = reason
                )
            )
            if (startRes.isSuccessful && startRes.body() != null) {
                val dto = startRes.body()!!
                apiService.completeRefile(dto.id)
                val location = Location(
                    id = dto.id,
                    barcode = destinationBoxBarcode,
                    name = destinationBoxBarcode,
                    room = "",
                    rack = "",
                    shelf = "",
                    type = LocationType.LOCATION
                )
                val srcBox = Box(id = dto.id, barcode = dto.currentLocation, description = "Source Box", location = location)
                val dstBox = Box(id = destinationBoxBarcode, barcode = destinationBoxBarcode, description = "Destination Box $destinationBoxBarcode", location = location)
                val file = FileRecord(id = dto.id, barcode = fileBarcode, title = dto.fileName ?: "File $fileBarcode", currentBox = srcBox, currentLocation = location)
                val action = RefileAction(
                    id = dto.id,
                    fileRecord = file,
                    sourceBox = srcBox,
                    destinationBox = dstBox,
                    status = RefileActionStatus.OVERRIDDEN,
                    timestamp = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.US).apply {
                        timeZone = java.util.TimeZone.getTimeZone("UTC")
                    }.format(java.util.Date()),
                    overrideReason = reason
                )
                Result.success(action)
            } else {
                Result.failure(Exception("Failed to override refile on server"))
            }
        } catch (e: Exception) {
            Result.failure(Exception(ErrorUtils.getFriendlyErrorMessage(e)))
        }
    }

    override suspend fun startSession(): Result<RefileSession> {
        val session = RefileSession(
            id = java.util.UUID.randomUUID().toString(),
            sessionId = "REF-${System.currentTimeMillis()}",
            startTime = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.US).apply {
                timeZone = java.util.TimeZone.getTimeZone("UTC")
            }.format(java.util.Date()),
            endTime = null,
            actions = emptyList(),
            status = SessionStatus.ACTIVE
        )
        return Result.success(session)
    }

    override suspend fun endSession(sessionId: String): Result<Unit> {
        return Result.success(Unit)
    }

    override suspend fun undoLastAction(sessionId: String): Result<Unit> {
        return Result.success(Unit)
    }

    override suspend fun syncRefileActionToQueue(action: RefileAction): Result<Unit> {
        return Result.success(Unit)
    }
}
