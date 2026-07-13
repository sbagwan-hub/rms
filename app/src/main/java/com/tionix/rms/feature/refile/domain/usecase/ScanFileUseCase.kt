package com.tionix.rms.feature.refile.domain.usecase

import com.tionix.rms.feature.refile.domain.model.FileRecord
import com.tionix.rms.feature.refile.domain.repository.RefileRepository
import javax.inject.Inject

class ScanFileUseCase @Inject constructor(
    private val repository: RefileRepository
) {
    suspend operator fun invoke(barcode: String): Result<FileRecord> {
        // TODO: BACKEND ENDPOINT PENDING
        return repository.scanFile(barcode)
    }
}
