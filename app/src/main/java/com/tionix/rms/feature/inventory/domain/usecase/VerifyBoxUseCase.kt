package com.tionix.rms.feature.inventory.domain.usecase

import com.tionix.rms.feature.inventory.domain.model.ScannedBox
import com.tionix.rms.feature.inventory.domain.repository.InventoryVerificationRepository
import javax.inject.Inject

class VerifyBoxUseCase @Inject constructor(
    private val repository: InventoryVerificationRepository
) {
    suspend operator fun invoke(barcode: String, verificationId: String): Result<ScannedBox> {
        // TODO: BACKEND ENDPOINT PENDING
        return repository.verifyBox(barcode, verificationId)
    }
}
