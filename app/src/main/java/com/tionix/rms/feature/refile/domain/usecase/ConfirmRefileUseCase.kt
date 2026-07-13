package com.tionix.rms.feature.refile.domain.usecase

import com.tionix.rms.feature.refile.domain.model.RefileAction
import com.tionix.rms.feature.refile.domain.repository.RefileRepository
import javax.inject.Inject

class ConfirmRefileUseCase @Inject constructor(
    private val repository: RefileRepository
) {
    suspend operator fun invoke(fileBarcode: String, destinationBoxBarcode: String): Result<RefileAction> {
        // TODO: BACKEND ENDPOINT PENDING
        val result = repository.confirmRefile(fileBarcode, destinationBoxBarcode)
        if (result.isSuccess) {
            repository.syncRefileActionToQueue(result.getOrNull()!!)
        }
        return result
    }
}
