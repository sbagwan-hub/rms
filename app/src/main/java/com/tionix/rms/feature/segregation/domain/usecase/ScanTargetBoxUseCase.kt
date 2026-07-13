package com.tionix.rms.feature.segregation.domain.usecase

import com.tionix.rms.feature.segregation.domain.model.Box
import com.tionix.rms.feature.segregation.domain.repository.SegregationRepository
import javax.inject.Inject

class ScanTargetBoxUseCase @Inject constructor(
    private val repository: SegregationRepository
) {
    suspend operator fun invoke(barcode: String): Result<Box> {
        // TODO: BACKEND ENDPOINT PENDING
        return repository.scanTargetBox(barcode)
    }
}
