package com.tionix.rms.feature.segregation.domain.usecase

import com.tionix.rms.feature.segregation.domain.model.SegregationSession
import com.tionix.rms.feature.segregation.domain.repository.SegregationRepository
import javax.inject.Inject

class StartSegregationSessionUseCase @Inject constructor(
    private val repository: SegregationRepository
) {
    suspend operator fun invoke(): Result<SegregationSession> {
        // TODO: BACKEND ENDPOINT PENDING
        return repository.startSegregationSession()
    }
}
