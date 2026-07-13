package com.tionix.rms.feature.refile.domain.usecase

import com.tionix.rms.feature.refile.domain.model.FileRecord
import com.tionix.rms.feature.refile.domain.repository.RefileRepository
import javax.inject.Inject

class GetHomeLocationUseCase @Inject constructor(
    private val repository: RefileRepository
) {
    suspend operator fun invoke(fileBarcode: String): Result<FileRecord> {
        // TODO: BACKEND ENDPOINT PENDING - Room-first lookup, API refresh
        return repository.getHomeLocation(fileBarcode)
    }
}
