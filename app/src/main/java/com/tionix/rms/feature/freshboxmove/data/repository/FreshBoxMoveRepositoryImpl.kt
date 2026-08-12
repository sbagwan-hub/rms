package com.tionix.rms.feature.freshboxmove.data.repository

import com.tionix.rms.core.network.ErrorUtils
import com.tionix.rms.data.repository.WorkflowRepository
import com.tionix.rms.feature.auth.data.local.AuthPreferences
import com.tionix.rms.feature.freshboxmove.data.local.FreshBoxDao
import com.tionix.rms.feature.freshboxmove.data.local.FreshBoxScanEntity
import com.tionix.rms.feature.freshboxmove.data.local.FreshBoxSessionEntity
import com.tionix.rms.feature.freshboxmove.data.remote.FreshBoxApi
import com.tionix.rms.feature.freshboxmove.data.remote.SubmitScanRequestDto
import com.tionix.rms.feature.freshboxmove.domain.repository.FreshBoxMoveRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.runBlocking
import retrofit2.HttpException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.UUID
import javax.inject.Inject

class FreshBoxMoveRepositoryImpl @Inject constructor(
    private val freshBoxDao: FreshBoxDao,
    private val freshBoxApi: FreshBoxApi,
    private val workflowRepository: WorkflowRepository,
    private val authPreferences: AuthPreferences,
) : FreshBoxMoveRepository {

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

            Result.success(sessionEntity)
        } catch (e: Exception) {
            Result.failure(Exception(ErrorUtils.getFriendlyErrorMessage(e)))
        }
    }

    override suspend fun submitScan(
        boxBarcode: String,
        locationBarcode: String,
        roomBarcode: String?,
        rackBarcode: String?,
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

            val payload = buildMap<String, Any?> {
                put("locationBarcode", locationBarcode)
                put("boxBarcodes", listOf(boxBarcode))
                put("latitude", gpsLat)
                put("longitude", gpsLng)
                put("performedAt", dateFormat.format(Date(now)))
                roomBarcode?.takeIf { it.isNotBlank() }?.let { put("roomBarcode", it) }
                rackBarcode?.takeIf { it.isNotBlank() }?.let { put("rackBarcode", it) }
            }

            val submitResult = workflowRepository.submitOrEnqueue(
                clientOpId = clientEventId,
                type = "FRESH_BOX",
                payload = payload
            ) {
                val serverSessionId = activeSession.serverSessionId
                    ?: throw java.io.IOException("Session not synced yet")
                val response = freshBoxApi.submitScan(
                    sessionId = serverSessionId,
                    request = SubmitScanRequestDto(
                        locationBarcode = locationBarcode,
                        boxBarcode = boxBarcode,
                        clientEventId = clientEventId,
                        roomBarcode = roomBarcode,
                        rackBarcode = rackBarcode,
                        gpsLat = gpsLat,
                        gpsLng = gpsLng,
                        scannedAt = dateFormat.format(Date(now))
                    )
                )
                if (!response.isSuccessful) {
                    throw HttpException(response)
                }
                freshBoxDao.markScanAsSynced(clientEventId)
                response.body()!!
            }

            if (submitResult.isFailure) {
                return Result.failure(submitResult.exceptionOrNull()!!)
            }

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
