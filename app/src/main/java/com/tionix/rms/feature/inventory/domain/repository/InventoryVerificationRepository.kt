package com.tionix.rms.feature.inventory.domain.repository

import com.tionix.rms.feature.inventory.domain.model.Box
import com.tionix.rms.feature.inventory.domain.model.InventoryVerification
import com.tionix.rms.feature.inventory.domain.model.ScannedBox
import com.tionix.rms.feature.inventory.domain.model.StartVerificationRequest

interface InventoryVerificationRepository {
    suspend fun getAssignedVerifications(): Result<List<InventoryVerification>>
    suspend fun startVerification(request: StartVerificationRequest): Result<InventoryVerification>
    suspend fun getExpectedBoxes(locationId: String): Result<List<Box>>
    suspend fun verifyBox(barcode: String, verificationId: String): Result<ScannedBox>
    suspend fun completeVerification(verificationId: String): Result<Unit>
    // TODO: BACKEND ENDPOINT PENDING - Sync with pending sync queue
    suspend fun syncVerificationToQueue(verificationId: String): Result<Unit>
}
