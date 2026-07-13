package com.tionix.rms.feature.transfer.domain.usecase

import com.tionix.rms.feature.transfer.domain.repository.TransferRepository
import javax.inject.Inject

class SubmitTransferUseCase @Inject constructor(
    private val repository: TransferRepository
) {
    suspend operator fun invoke(sessionId: String): Result<Unit> {
        // TODO: BACKEND ENDPOINT PENDING
        val result = repository.submitTransfer(sessionId)
        if (result.isSuccess) {
            repository.syncTransferToQueue(sessionId)
        }
        return result
    }
}
