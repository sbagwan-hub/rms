package com.tionix.rms.feature.transfer.domain.usecase

import com.tionix.rms.feature.transfer.domain.repository.TransferRepository
import javax.inject.Inject

class SetDestinationUseCase @Inject constructor(
    private val repository: TransferRepository
) {
    suspend operator fun invoke(sessionId: String, destination: String): Result<Unit> {
        // TODO: BACKEND ENDPOINT PENDING
        return repository.setDestination(sessionId, destination)
    }
}
