package com.tionix.rms.feature.transfer.domain.usecase

import com.tionix.rms.feature.transfer.domain.model.TransferSession
import com.tionix.rms.feature.transfer.domain.repository.TransferRepository
import javax.inject.Inject

class StartTransferSessionUseCase @Inject constructor(
    private val repository: TransferRepository
) {
    suspend operator fun invoke(transferType: com.tionix.rms.feature.transfer.domain.model.TransferType): Result<TransferSession> {
        // TODO: BACKEND ENDPOINT PENDING
        return repository.startTransferSession(transferType)
    }
}
