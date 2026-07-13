package com.tionix.rms.feature.merge.domain.usecase

import com.tionix.rms.feature.merge.domain.repository.MergeRepository
import javax.inject.Inject

class RemoveSourceBoxUseCase @Inject constructor(
    private val repository: MergeRepository
) {
    suspend operator fun invoke(sessionId: String, boxBarcode: String): Result<Unit> {
        // TODO: BACKEND ENDPOINT PENDING
        return repository.removeSourceBox(sessionId, boxBarcode)
    }
}
