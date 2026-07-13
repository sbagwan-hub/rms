package com.tionix.rms.feature.segregation.domain.repository

import com.tionix.rms.feature.segregation.domain.model.Box
import com.tionix.rms.feature.segregation.domain.model.FileRecord
import com.tionix.rms.feature.segregation.domain.model.Segregation
import com.tionix.rms.feature.segregation.domain.model.SegregationSession
import com.tionix.rms.feature.segregation.domain.model.StartSegregationRequest

interface SegregationRepository {
    suspend fun getAssignedSegregations(): Result<List<Segregation>>
    suspend fun startSegregation(request: StartSegregationRequest): Result<Segregation>
    suspend fun completeSegregation(segregationId: String): Result<Unit>
    suspend fun scanBox(barcode: String): Result<Segregation?>
    suspend fun startSegregationSession(): Result<SegregationSession>
    suspend fun scanSourceBox(barcode: String): Result<Box>
    suspend fun scanTargetBox(barcode: String): Result<Box>
    suspend fun moveFile(fileBarcode: String): Result<FileRecord>
    suspend fun completeSegregationSession(sessionId: String): Result<Unit>
    suspend fun syncSegregationToQueue(sessionId: String): Result<Unit>
}
