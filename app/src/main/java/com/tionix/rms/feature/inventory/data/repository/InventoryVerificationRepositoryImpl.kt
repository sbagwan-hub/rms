package com.tionix.rms.feature.inventory.data.repository

import com.google.gson.Gson
import com.tionix.rms.core.network.ErrorUtils
import com.tionix.rms.core.sync.data.local.SyncOperationDao
import com.tionix.rms.core.sync.data.local.SyncOperationEntity
import com.tionix.rms.feature.auth.data.local.AuthPreferences
import com.tionix.rms.feature.inventory.data.local.*
import com.tionix.rms.feature.inventory.data.remote.InventoryApiService
import com.tionix.rms.feature.inventory.data.remote.dto.*
import com.tionix.rms.feature.inventory.domain.model.Box
import com.tionix.rms.feature.inventory.domain.model.BoxStatus
import com.tionix.rms.feature.inventory.domain.model.InventoryVerification
import com.tionix.rms.feature.inventory.domain.model.ScannedBox
import com.tionix.rms.feature.inventory.domain.model.ScanStatus
import com.tionix.rms.feature.inventory.domain.model.StartVerificationRequest
import com.tionix.rms.feature.inventory.domain.model.VerificationStatus
import com.tionix.rms.feature.inventory.domain.repository.InventoryVerificationRepository
import kotlinx.coroutines.runBlocking
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.UUID
import javax.inject.Inject

class InventoryVerificationRepositoryImpl @Inject constructor(
    private val apiService: InventoryApiService,
    private val inventoryDao: InventoryDao,
    private val syncOperationDao: SyncOperationDao,
    private val authPreferences: AuthPreferences
) : InventoryVerificationRepository {

    private val gson = Gson()
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    override suspend fun getAssignedVerifications(): Result<List<InventoryVerification>> {
        return try {
            val response = apiService.getAssignedVerifications()
            if (response.isSuccessful && response.body() != null) {
                val sessions = response.body()!!
                // Sync to local DB
                sessions.forEach { dto ->
                    inventoryDao.insertSession(
                        InventoryVerificationSessionEntity(
                            clientSessionId = dto.id,
                            serverSessionId = dto.id,
                            boxId = dto.boxId,
                            boxBarcode = dto.box.barcode,
                            operatorId = dto.operatorId,
                            startedAt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).parse(dto.startedAt)?.time ?: System.currentTimeMillis(),
                            endedAt = dto.endedAt?.let { SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).parse(it)?.time },
                            missingFileCount = dto.missingFileCount,
                            unexpectedFileCount = dto.unexpectedFileCount
                        )
                    )
                }
                Result.success(sessions.map { it.toDomain() })
            } else {
                loadLocalSessions()
            }
        } catch (e: Exception) {
            loadLocalSessions()
        }
    }

    private suspend fun loadLocalSessions(): Result<List<InventoryVerification>> {
        return try {
            Result.success(emptyList())
        } catch (e: Exception) {
            Result.failure(Exception(ErrorUtils.getFriendlyErrorMessage(e)))
        }
    }

    override suspend fun startVerification(request: StartVerificationRequest): Result<InventoryVerification> {
        val clientSessionId = UUID.randomUUID().toString()
        val operatorId = runBlocking { authPreferences.getUserId() ?: "unknown" }
        val now = System.currentTimeMillis()
        val boxBarcode = request.locationId

        return try {
            val boxDetailsResponse = apiService.resolveBoxBarcode(boxBarcode)
            if (!boxDetailsResponse.isSuccessful || boxDetailsResponse.body() == null) {
                return Result.failure(Exception("Failed to resolve box barcode: ${boxDetailsResponse.message()}"))
            }
            val boxDetails = boxDetailsResponse.body()!!
            val boxId = boxDetails.id

            val filesResponse = apiService.getFilesByBox(boxId)
            if (filesResponse.isSuccessful && filesResponse.body() != null) {
                val files = filesResponse.body()!!
                inventoryDao.clearExpectedFiles(boxId)
                inventoryDao.insertExpectedFiles(
                    files.map {
                        InventoryExpectedFileEntity(
                            id = it.id,
                            boxId = it.boxId,
                            barcode = it.barcode,
                            title = it.title,
                            status = it.status
                        )
                    }
                )
            }

            val startResponse = apiService.startVerification(StartVerificationRequestDto(boxId = boxId))
            if (startResponse.isSuccessful && startResponse.body() != null) {
                val serverSession = startResponse.body()!!
                
                val sessionEntity = InventoryVerificationSessionEntity(
                    clientSessionId = clientSessionId,
                    serverSessionId = serverSession.id,
                    boxId = boxId,
                    boxBarcode = boxBarcode,
                    operatorId = operatorId,
                    startedAt = now,
                    endedAt = null,
                    missingFileCount = 0,
                    unexpectedFileCount = 0
                )
                inventoryDao.insertSession(sessionEntity)

                Result.success(serverSession.toDomain())
            } else {
                Result.failure(Exception("Failed to start verification on server"))
            }
        } catch (e: Exception) {
            val boxId = boxBarcode
            val sessionEntity = InventoryVerificationSessionEntity(
                clientSessionId = clientSessionId,
                serverSessionId = null,
                boxId = boxId,
                boxBarcode = boxBarcode,
                operatorId = operatorId,
                startedAt = now,
                endedAt = null,
                missingFileCount = 0,
                unexpectedFileCount = 0
            )
            inventoryDao.insertSession(sessionEntity)

            val syncOperation = SyncOperationEntity(
                id = UUID.randomUUID().toString(),
                type = "START_INVENTORY_VERIFY_SESSION",
                endpoint = "workflows/inventory-verify/sessions",
                payload = gson.toJson(mapOf(
                    "clientSessionId" to clientSessionId,
                    "boxBarcode" to boxBarcode
                )),
                status = "PENDING",
                retryCount = 0,
                createdAt = dateFormat.format(Date(now)),
                errorMessage = null
            )
            syncOperationDao.insert(syncOperation)

            Result.success(
                InventoryVerification(
                    id = clientSessionId,
                    verificationCode = "INV-${clientSessionId.take(8).uppercase()}",
                    locationId = boxId,
                    locationName = boxBarcode,
                    status = VerificationStatus.IN_PROGRESS,
                    totalBoxes = 0,
                    verifiedBoxes = 0,
                    discrepancyBoxes = 0,
                    assignedTo = operatorId,
                    startedAt = dateFormat.format(Date(now)),
                    completedAt = null,
                    createdAt = dateFormat.format(Date(now))
                )
            )
        }
    }

    override suspend fun getExpectedBoxes(locationId: String): Result<List<Box>> {
        return try {
            val expectedFiles = inventoryDao.getExpectedFiles(locationId)
            val domainBoxes = expectedFiles.map {
                Box(
                    id = it.id,
                    barcode = it.barcode,
                    description = it.title,
                    currentLocation = it.boxId,
                    expectedLocation = it.boxId,
                    status = BoxStatus.PENDING
                )
            }
            Result.success(domainBoxes)
        } catch (e: Exception) {
            Result.failure(Exception(ErrorUtils.getFriendlyErrorMessage(e)))
        }
    }

    override suspend fun verifyBox(barcode: String, verificationId: String): Result<ScannedBox> {
        val clientEventId = UUID.randomUUID().toString()
        val now = System.currentTimeMillis()

        val activeSession = inventoryDao.getActiveSession()
        val isExpected = if (activeSession != null) {
            val expectedFiles = inventoryDao.getExpectedFiles(activeSession.boxId)
            expectedFiles.any { it.barcode == barcode }
        } else {
            true
        }

        return try {
            if (activeSession != null) {
                inventoryDao.insertScan(
                    InventoryVerificationScanEntity(
                        clientEventId = clientEventId,
                        clientSessionId = activeSession.clientSessionId,
                        fileBarcode = barcode,
                        scannedAt = now,
                        isExpected = isExpected,
                        isSynced = false
                    )
                )
            }

            val sessionId = activeSession?.serverSessionId ?: verificationId
            val syncOperation = SyncOperationEntity(
                id = UUID.randomUUID().toString(),
                type = "SUBMIT_INVENTORY_VERIFY_SCAN",
                endpoint = "workflows/inventory-verify/sessions/{sessionId}/scans",
                payload = gson.toJson(mapOf(
                    "clientEventId" to clientEventId,
                    "sessionId" to sessionId,
                    "fileBarcode" to barcode,
                    "scannedAt" to dateFormat.format(Date(now))
                )),
                status = "PENDING",
                retryCount = 0,
                createdAt = dateFormat.format(Date(now)),
                errorMessage = null
            )
            syncOperationDao.insert(syncOperation)

            val response = apiService.scanBox(
                id = sessionId,
                request = SubmitVerifyScanRequestDto(
                    fileBarcode = barcode,
                    clientEventId = clientEventId,
                    scannedAt = dateFormat.format(Date(now))
                )
            )

            if (response.isSuccessful && response.body() != null) {
                val scanDto = response.body()!!
                inventoryDao.markScanAsSynced(clientEventId)
                
                Result.success(
                    ScannedBox(
                        barcode = barcode,
                        scanStatus = if (scanDto.isExpected) ScanStatus.VERIFIED else ScanStatus.UNEXPECTED,
                        timestamp = scanDto.scannedAt
                    )
                )
            } else {
                Result.success(
                    ScannedBox(
                        barcode = barcode,
                        scanStatus = if (isExpected) ScanStatus.VERIFIED else ScanStatus.UNEXPECTED,
                        timestamp = dateFormat.format(Date(now))
                    )
                )
            }
        } catch (e: Exception) {
            Result.success(
                ScannedBox(
                    barcode = barcode,
                    scanStatus = if (isExpected) ScanStatus.VERIFIED else ScanStatus.UNEXPECTED,
                    timestamp = dateFormat.format(Date(now))
                )
            )
        }
    }

    override suspend fun completeVerification(verificationId: String): Result<Unit> {
        val now = System.currentTimeMillis()
        val activeSession = inventoryDao.getActiveSession()
            ?: return Result.failure(IllegalStateException("No active inventory verification session found"))

        val expectedFiles = inventoryDao.getExpectedFiles(activeSession.boxId)
        val scans = inventoryDao.getScansForSession(activeSession.clientSessionId)
        
        val scannedBarcodes = scans.map { it.fileBarcode }.toSet()
        val missingCount = expectedFiles.count { !scannedBarcodes.contains(it.barcode) }
        val unexpectedCount = scans.count { !it.isExpected }

        return try {
            inventoryDao.endSession(activeSession.clientSessionId, now, missingCount, unexpectedCount)

            val sessionId = activeSession.serverSessionId ?: verificationId
            val syncOperation = SyncOperationEntity(
                id = UUID.randomUUID().toString(),
                type = "END_INVENTORY_VERIFY_SESSION",
                endpoint = "workflows/inventory-verify/sessions/{sessionId}/end",
                payload = gson.toJson(mapOf(
                    "clientSessionId" to activeSession.clientSessionId,
                    "sessionId" to sessionId
                )),
                status = "PENDING",
                retryCount = 0,
                createdAt = dateFormat.format(Date(now)),
                errorMessage = null
            )
            syncOperationDao.insert(syncOperation)

            val response = apiService.completeVerification(sessionId)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.success(Unit)
            }
        } catch (e: Exception) {
            Result.success(Unit)
        }
    }

    override suspend fun syncVerificationToQueue(verificationId: String): Result<Unit> {
        return Result.success(Unit)
    }
}
