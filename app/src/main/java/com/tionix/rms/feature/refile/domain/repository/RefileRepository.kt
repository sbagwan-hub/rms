package com.tionix.rms.feature.refile.domain.repository

import com.tionix.rms.feature.refile.domain.model.FileRecord
import com.tionix.rms.feature.refile.domain.model.Refile
import com.tionix.rms.feature.refile.domain.model.RefileAction
import com.tionix.rms.feature.refile.domain.model.RefileSession
import com.tionix.rms.feature.refile.domain.model.StartRefileRequest

interface RefileRepository {
    suspend fun getAssignedRefiles(): Result<List<Refile>>
    suspend fun startRefile(request: StartRefileRequest): Result<Refile>
    suspend fun completeRefile(refileId: String): Result<Unit>
    suspend fun scanFile(barcode: String): Result<FileRecord>
    suspend fun getHomeLocation(fileBarcode: String): Result<FileRecord>
    suspend fun confirmRefile(fileBarcode: String, destinationBoxBarcode: String): Result<RefileAction>
    suspend fun overrideMismatch(fileBarcode: String, destinationBoxBarcode: String, reason: String): Result<RefileAction>
    suspend fun startSession(): Result<RefileSession>
    suspend fun endSession(sessionId: String): Result<Unit>
    suspend fun undoLastAction(sessionId: String): Result<Unit>
    // TODO: BACKEND ENDPOINT PENDING - Sync with pending sync queue
    suspend fun syncRefileActionToQueue(action: RefileAction): Result<Unit>
}
