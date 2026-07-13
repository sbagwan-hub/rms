package com.tionix.rms.feature.merge.domain.usecase

import com.tionix.rms.feature.merge.domain.model.Box
import com.tionix.rms.feature.merge.domain.repository.MergeRepository
import javax.inject.Inject

class ScanSourceBoxUseCase @Inject constructor(
    private val repository: MergeRepository
) {
    suspend operator fun invoke(sessionId: String, barcode: String): Result<Box> {
        // TODO: BACKEND ENDPOINT PENDING
        return repository.scanSourceBox(sessionId, barcode)
    }
}
