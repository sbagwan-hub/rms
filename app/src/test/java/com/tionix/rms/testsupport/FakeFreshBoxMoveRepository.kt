package com.tionix.rms.testsupport

import com.tionix.rms.feature.freshboxmove.data.local.FreshBoxScanEntity
import com.tionix.rms.feature.freshboxmove.data.local.FreshBoxSessionEntity
import com.tionix.rms.feature.freshboxmove.domain.repository.FreshBoxMoveRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

class FakeFreshBoxMoveRepository : FreshBoxMoveRepository {
    var activeSession: FreshBoxSessionEntity? = null
    private val scansBySession = mutableMapOf<String, MutableList<FreshBoxScanEntity>>()
    private val scanFlows = mutableMapOf<String, MutableStateFlow<List<FreshBoxScanEntity>>>()

    override suspend fun startSession(deviceId: String?): Result<FreshBoxSessionEntity> {
        val session = FreshBoxSessionEntity(
            clientSessionId = UUID.randomUUID().toString(),
            serverSessionId = null,
            operatorId = "operator-1",
            startedAt = System.currentTimeMillis(),
            endedAt = null,
            deviceId = deviceId
        )
        activeSession = session
        scansBySession[session.clientSessionId] = mutableListOf()
        scanFlows[session.clientSessionId] = MutableStateFlow(emptyList())
        return Result.success(session)
    }

    override suspend fun submitScan(
        boxBarcode: String,
        locationBarcode: String,
        roomBarcode: String?,
        rackBarcode: String?,
        gpsLat: Double?,
        gpsLng: Double?
    ): Result<FreshBoxScanEntity> {
        val session = activeSession ?: return Result.failure(IllegalStateException("No session"))
        val entity = FreshBoxScanEntity(
            clientEventId = UUID.randomUUID().toString(),
            clientSessionId = session.clientSessionId,
            boxBarcode = boxBarcode,
            locationBarcode = locationBarcode,
            scannedAt = System.currentTimeMillis(),
            gpsLat = gpsLat,
            gpsLng = gpsLng,
            isSynced = false
        )
        scansBySession.getValue(session.clientSessionId).add(entity)
        scanFlows.getValue(session.clientSessionId).value =
            scansBySession.getValue(session.clientSessionId).toList()
        return Result.success(entity)
    }

    override suspend fun endSession(): Result<Unit> {
        activeSession = null
        return Result.success(Unit)
    }

    override suspend fun getActiveSession(): FreshBoxSessionEntity? = activeSession

    override fun getScansForSessionFlow(sessionId: String): Flow<List<FreshBoxScanEntity>> {
        return scanFlows.getOrPut(sessionId) { MutableStateFlow(emptyList()) }.asStateFlow()
    }

    fun scansForActiveSession(): List<FreshBoxScanEntity> {
        val session = activeSession ?: return emptyList()
        return scansBySession[session.clientSessionId].orEmpty()
    }
}
