package com.tionix.rms.feature.inventory.domain.usecase

import com.tionix.rms.feature.inventory.domain.model.Box
import com.tionix.rms.feature.inventory.domain.repository.InventoryVerificationRepository
import javax.inject.Inject

class GetExpectedBoxesUseCase @Inject constructor(
    private val repository: InventoryVerificationRepository
) {
    suspend operator fun invoke(locationId: String): Result<List<Box>> {
        // TODO: BACKEND ENDPOINT PENDING
        return repository.getExpectedBoxes(locationId)
    }
}
