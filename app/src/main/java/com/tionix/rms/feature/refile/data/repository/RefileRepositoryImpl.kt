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

    override suspend fun scanFile(barcode: String): Result<FileRecord> {
        val location = Location(
            id = "loc-1",
            barcode = "LOC-WH1",
            name = "Warehouse Location",
            room = "Main Room",
            rack = "Rack 1",
            shelf = "Shelf A",
            type = LocationType.LOCATION
        )
        val box = Box(
            id = "box-1",
            barcode = "BOX-DEFAULT",
            description = "Home Box",
            location = location
        )
        val fileRecord = FileRecord(
            id = barcode,
            barcode = barcode,
            title = "File $barcode",
            currentBox = box,
            currentLocation = location
        )
        return Result.success(fileRecord)
    }

    // Refile workflow methods — use-case layer manages local session state
    override suspend fun getHomeLocation(fileBarcode: String): Result<FileRecord> {
        return scanFile(fileBarcode)
    }

    override suspend fun confirmRefile(
        fileBarcode: String,
        destinationBoxBarcode: String
    ): Result<RefileAction> {
        val location = Location(
            id = "loc-1",
            barcode = "LOC-WH1",
            name = "Warehouse Location",
            room = "Main Room",
            rack = "Rack 1",
            shelf = "Shelf A",
            type = LocationType.LOCATION
        )
        val srcBox = Box(id = "src-1", barcode = "BOX-SRC", description = "Source Box", location = location)
        val dstBox = Box(id = destinationBoxBarcode, barcode = destinationBoxBarcode, description = "Destination Box $destinationBoxBarcode", location = location)
        val file = FileRecord(id = fileBarcode, barcode = fileBarcode, title = "File $fileBarcode", currentBox = srcBox, currentLocation = location)
        val action = RefileAction(
            id = java.util.UUID.randomUUID().toString(),
            fileRecord = file,
            sourceBox = srcBox,
            destinationBox = dstBox,
            status = RefileActionStatus.CONFIRMED,
            timestamp = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.US).apply {
                timeZone = java.util.TimeZone.getTimeZone("UTC")
            }.format(java.util.Date()),
            overrideReason = null
        )
        return Result.success(action)
    }

    override suspend fun overrideMismatch(
        fileBarcode: String,
        destinationBoxBarcode: String,
        reason: String
    ): Result<RefileAction> {
        val location = Location(
            id = "loc-1",
            barcode = "LOC-WH1",
            name = "Warehouse Location",
            room = "Main Room",
            rack = "Rack 1",
            shelf = "Shelf A",
            type = LocationType.LOCATION
        )
        val srcBox = Box(id = "src-1", barcode = "BOX-SRC", description = "Source Box", location = location)
        val dstBox = Box(id = destinationBoxBarcode, barcode = destinationBoxBarcode, description = "Destination Box $destinationBoxBarcode", location = location)
        val file = FileRecord(id = fileBarcode, barcode = fileBarcode, title = "File $fileBarcode", currentBox = srcBox, currentLocation = location)
        val action = RefileAction(
            id = java.util.UUID.randomUUID().toString(),
            fileRecord = file,
            sourceBox = srcBox,
            destinationBox = dstBox,
            status = RefileActionStatus.OVERRIDDEN,
            timestamp = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.US).apply {
                timeZone = java.util.TimeZone.getTimeZone("UTC")
            }.format(java.util.Date()),
            overrideReason = reason
        )
        return Result.success(action)
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
