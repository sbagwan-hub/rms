package com.tionix.rms.feature.inventory.domain.usecase

import com.tionix.rms.feature.inventory.domain.repository.InventoryVerificationRepository
import javax.inject.Inject

class CompleteVerificationUseCase @Inject constructor(
    private val repository: InventoryVerificationRepository
) {
    suspend operator fun invoke(verificationId: String): Result<Unit> {
        val result = repository.completeVerification(verificationId)
        if (result.isSuccess) {
            // TODO: BACKEND ENDPOINT PENDING - Sync with pending sync queue
            repository.syncVerificationToQueue(verificationId)
        }
        return result
    }
}
