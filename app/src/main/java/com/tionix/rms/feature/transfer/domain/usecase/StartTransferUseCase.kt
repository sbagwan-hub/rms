package com.tionix.rms.feature.transfer.domain.usecase

import com.tionix.rms.feature.transfer.domain.model.Transfer
import com.tionix.rms.feature.transfer.domain.model.StartTransferRequest
import com.tionix.rms.feature.transfer.domain.repository.TransferRepository

class StartTransferUseCase(private val repository: TransferRepository) {
    suspend operator fun invoke(request: StartTransferRequest): Result<Transfer> {
        return repository.startTransfer(request)
    }
}
