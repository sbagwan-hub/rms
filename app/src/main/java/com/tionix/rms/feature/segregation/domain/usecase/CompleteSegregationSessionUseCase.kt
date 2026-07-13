package com.tionix.rms.feature.segregation.domain.usecase

import com.tionix.rms.feature.segregation.domain.repository.SegregationRepository
import javax.inject.Inject

class CompleteSegregationSessionUseCase @Inject constructor(
    private val repository: SegregationRepository
) {
    suspend operator fun invoke(sessionId: String): Result<Unit> {
        // TODO: BACKEND ENDPOINT PENDING
        val result = repository.completeSegregationSession(sessionId)
        if (result.isSuccess) {
            repository.syncSegregationToQueue(sessionId)
        }
        return result
    }
}
