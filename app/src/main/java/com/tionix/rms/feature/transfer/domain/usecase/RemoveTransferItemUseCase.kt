package com.tionix.rms.feature.transfer.domain.usecase

import com.tionix.rms.feature.transfer.domain.repository.TransferRepository
import javax.inject.Inject

class RemoveTransferItemUseCase @Inject constructor(
    private val repository: TransferRepository
) {
    suspend operator fun invoke(sessionId: String, itemId: String): Result<Unit> {
        // TODO: BACKEND ENDPOINT PENDING
        return repository.removeTransferItem(sessionId, itemId)
    }
}
