package com.tionix.rms.feature.transfer.domain.usecase

import com.tionix.rms.feature.transfer.domain.model.TransferItem
import com.tionix.rms.feature.transfer.domain.repository.TransferRepository
import javax.inject.Inject

class AddTransferItemUseCase @Inject constructor(
    private val repository: TransferRepository
) {
    suspend operator fun invoke(sessionId: String, barcode: String): Result<TransferItem> {
        // TODO: BACKEND ENDPOINT PENDING
        return repository.addTransferItem(sessionId, barcode)
    }
}
