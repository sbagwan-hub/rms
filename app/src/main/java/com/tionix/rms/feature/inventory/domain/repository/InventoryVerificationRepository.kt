package com.tionix.rms.feature.inventory.domain.repository

import com.tionix.rms.feature.inventory.domain.model.InventoryVerification
import com.tionix.rms.feature.inventory.domain.model.StartVerificationRequest

interface InventoryVerificationRepository {
    suspend fun getAssignedVerifications(): Result<List<InventoryVerification>>
    suspend fun startVerification(request: StartVerificationRequest): Result<InventoryVerification>
    suspend fun completeVerification(verificationId: String): Result<Unit>
    suspend fun scanBox(barcode: String, verificationId: String): Result<Unit>
}
