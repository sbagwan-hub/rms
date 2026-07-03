package com.tionix.rms.feature.segregation.domain.repository

import com.tionix.rms.feature.segregation.domain.model.Segregation
import com.tionix.rms.feature.segregation.domain.model.StartSegregationRequest

interface SegregationRepository {
    suspend fun getAssignedSegregations(): Result<List<Segregation>>
    suspend fun startSegregation(request: StartSegregationRequest): Result<Segregation>
    suspend fun completeSegregation(segregationId: String): Result<Unit>
    suspend fun scanBox(barcode: String): Result<Segregation?>
}
