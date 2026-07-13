package com.tionix.rms.feature.refile.domain.usecase

import com.tionix.rms.feature.refile.domain.model.RefileAction
import com.tionix.rms.feature.refile.domain.repository.RefileRepository
import javax.inject.Inject

class OverrideMismatchUseCase @Inject constructor(
    private val repository: RefileRepository
) {
    suspend operator fun invoke(fileBarcode: String, destinationBoxBarcode: String, reason: String): Result<RefileAction> {
        // TODO: BACKEND ENDPOINT PENDING - Override ONLY for SUPERVISOR/WAREHOUSE_MANAGER
        val result = repository.overrideMismatch(fileBarcode, destinationBoxBarcode, reason)
        if (result.isSuccess) {
            repository.syncRefileActionToQueue(result.getOrNull()!!)
        }
        return result
    }
}
