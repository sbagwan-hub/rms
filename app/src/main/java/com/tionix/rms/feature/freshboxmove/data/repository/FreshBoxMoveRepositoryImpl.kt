package com.tionix.rms.feature.freshboxmove.data.repository

import com.google.gson.Gson
import com.tionix.rms.core.network.ErrorUtils
import com.tionix.rms.core.sync.data.local.SyncOperationDao
import com.tionix.rms.core.sync.data.local.SyncOperationEntity
import com.tionix.rms.feature.auth.data.local.AuthPreferences
import com.tionix.rms.feature.freshboxmove.data.local.FreshBoxDao
import com.tionix.rms.feature.freshboxmove.data.local.FreshBoxScanEntity
import com.tionix.rms.feature.freshboxmove.data.local.FreshBoxSessionEntity
import com.tionix.rms.feature.freshboxmove.domain.repository.FreshBoxMoveRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.runBlocking
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.UUID
import javax.inject.Inject

class FreshBoxMoveRepositoryImpl @Inject constructor(
    private val freshBoxDao: FreshBoxDao,
    private val syncOperationDao: SyncOperationDao,
    private val authPreferences: AuthPreferences
) : FreshBoxMoveRepository {

    private val gson = Gson()
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    override suspend fun startSession(deviceId: String?): Result<FreshBoxSessionEntity> {
        return try {
            val clientSessionId = UUID.randomUUID().toString()
            val operatorId = runBlocking { authPreferences.getUserId() ?: "unknown" }
            val now = System.currentTimeMillis()

            val sessionEntity = FreshBoxSessionEntity(
                clientSessionId = clientSessionId,
                serverSessionId = null,
                operatorId = operatorId,
                startedAt = now,
                endedAt = null,
                deviceId = deviceId
            )

            // 1. Write session to Room
            freshBoxDao.insertSession(sessionEntity)

            // 2. Queue in Pending Sync Queue
            val syncOperation = SyncOperationEntity(
                id = UUID.randomUUID().toString(),
                type = "START_FRESH_BOX_SESSION",
                endpoint = "workflows/fresh-box-move/sessions",
                payload = gson.toJson(mapOf(
                    "clientSessionId" to clientSessionId,
                    "deviceId" to deviceId
                )),
                status = "PENDING",
                retryCount = 0,
                createdAt = dateFormat.format(Date(now)),
                errorMessage = null
            )
            syncOperationDao.insert(syncOperation)

            Result.success(sessionEntity)
        } catch (e: Exception) {
            Result.failure(Exception(ErrorUtils.getFriendlyErrorMessage(e)))
        }
    }

    override suspend fun submitScan(
        boxBarcode: String,
        locationBarcode: String,
        gpsLat: Double?,
        gpsLng: Double?
    ): Result<FreshBoxScanEntity> {
        return try {
            val activeSession = freshBoxDao.getActiveSession()
                ?: return Result.failure(IllegalStateException("No active Fresh Box session found"))

            val clientEventId = UUID.randomUUID().toString()
            val now = System.currentTimeMillis()

            val scanEntity = FreshBoxScanEntity(
                clientEventId = clientEventId,
                clientSessionId = activeSession.clientSessionId,
                boxBarcode = boxBarcode,
                locationBarcode = locationBarcode,
                scannedAt = now,
                gpsLat = gpsLat,
                gpsLng = gpsLng,
                isSynced = false
            )

            // 1. Write scan locally to Room
            freshBoxDao.insertScan(scanEntity)

            // 2. Queue in Pending Sync Queue
            val syncOperation = SyncOperationEntity(
                id = UUID.randomUUID().toString(),
                type = "SUBMIT_FRESH_BOX_SCAN",
                endpoint = "workflows/fresh-box-move/sessions/{sessionId}/scans",
                payload = gson.toJson(mapOf(
                    "clientEventId" to clientEventId,
                    "clientSessionId" to activeSession.clientSessionId,
                    "boxBarcode" to boxBarcode,
                    "locationBarcode" to locationBarcode,
                    "gpsLat" to gpsLat,
                    "gpsLng" to gpsLng,
                    "scannedAt" to dateFormat.format(Date(now))
                )),
                status = "PENDING",
                retryCount = 0,
                createdAt = dateFormat.format(Date(now)),
                errorMessage = null
            )
            syncOperationDao.insert(syncOperation)

            Result.success(scanEntity)
        } catch (e: Exception) {
            Result.failure(Exception(ErrorUtils.getFriendlyErrorMessage(e)))
        }
    }

    override suspend fun endSession(): Result<Unit> {
        return try {
            val activeSession = freshBoxDao.getActiveSession()
                ?: return Result.failure(IllegalStateException("No active Fresh Box session found"))

            val now = System.currentTimeMillis()

            // 1. End session locally
            freshBoxDao.endSession(activeSession.clientSessionId, now)

            // 2. Queue end session operation
            val syncOperation = SyncOperationEntity(
                id = UUID.randomUUID().toString(),
                type = "END_FRESH_BOX_SESSION",
                endpoint = "workflows/fresh-box-move/sessions/{sessionId}/end",
                payload = gson.toJson(mapOf(
                    "clientSessionId" to activeSession.clientSessionId
                )),
                status = "PENDING",
                retryCount = 0,
                createdAt = dateFormat.format(Date(now)),
                errorMessage = null
            )
            syncOperationDao.insert(syncOperation)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception(ErrorUtils.getFriendlyErrorMessage(e)))
        }
    }

    override suspend fun getActiveSession(): FreshBoxSessionEntity? {
        return freshBoxDao.getActiveSession()
    }

    override fun getScansForSessionFlow(sessionId: String): Flow<List<FreshBoxScanEntity>> {
        return freshBoxDao.getScansForSessionFlow(sessionId)
    }
}
