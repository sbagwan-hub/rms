package com.tionix.rms.feature.refile.domain.usecase

import com.tionix.rms.feature.refile.domain.model.RefileSession
import com.tionix.rms.feature.refile.domain.repository.RefileRepository
import javax.inject.Inject

class StartSessionUseCase @Inject constructor(
    private val repository: RefileRepository
) {
    suspend operator fun invoke(): Result<RefileSession> {
        // TODO: BACKEND ENDPOINT PENDING
        return repository.startSession()
    }
}
