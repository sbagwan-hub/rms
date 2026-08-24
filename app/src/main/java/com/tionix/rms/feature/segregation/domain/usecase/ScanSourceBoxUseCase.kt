package com.tionix.rms.feature.segregation.domain.usecase

import com.tionix.rms.feature.segregation.domain.model.Box
import com.tionix.rms.feature.segregation.domain.repository.SegregationRepository
import javax.inject.Inject

class ScanSourceBoxUseCase @Inject constructor(
    private val repository: SegregationRepository
) {
    suspend operator fun invoke(sessionId: String = "", barcode: String): Result<Box> {
        return repository.scanSourceBox(sessionId, barcode)
    }
}
