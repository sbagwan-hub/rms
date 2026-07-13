package com.tionix.rms.feature.segregation.domain.usecase

import com.tionix.rms.feature.segregation.domain.model.FileRecord
import com.tionix.rms.feature.segregation.domain.repository.SegregationRepository
import javax.inject.Inject

class MoveFileUseCase @Inject constructor(
    private val repository: SegregationRepository
) {
    suspend operator fun invoke(fileBarcode: String): Result<FileRecord> {
        // TODO: BACKEND ENDPOINT PENDING
        return repository.moveFile(fileBarcode)
    }
}
