package com.tionix.rms.feature.merge.domain.usecase

import com.tionix.rms.feature.merge.domain.model.Box
import com.tionix.rms.feature.merge.domain.repository.MergeRepository
import javax.inject.Inject

class ScanDestinationBoxUseCase @Inject constructor(
    private val repository: MergeRepository
) {
    suspend operator fun invoke(barcode: String): Result<Box> {
        // TODO: BACKEND ENDPOINT PENDING
        return repository.scanDestinationBox(barcode)
    }
}
