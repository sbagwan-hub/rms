package com.tionix.rms.feature.inventory.domain.usecase

import com.tionix.rms.feature.inventory.domain.model.InventoryVerification
import com.tionix.rms.feature.inventory.domain.model.StartVerificationRequest
import com.tionix.rms.feature.inventory.domain.repository.InventoryVerificationRepository
import javax.inject.Inject

class StartVerificationUseCase @Inject constructor(private val repository: InventoryVerificationRepository) {
    suspend operator fun invoke(request: StartVerificationRequest): Result<InventoryVerification> {
        return repository.startVerification(request)
    }
}
