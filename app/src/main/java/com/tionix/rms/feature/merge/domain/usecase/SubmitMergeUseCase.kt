package com.tionix.rms.feature.merge.domain.usecase

import com.tionix.rms.feature.merge.domain.repository.MergeRepository
import javax.inject.Inject

class SubmitMergeUseCase @Inject constructor(
    private val repository: MergeRepository
) {
    suspend operator fun invoke(sessionId: String): Result<Unit> {
        // TODO: BACKEND ENDPOINT PENDING
        val result = repository.submitMerge(sessionId)
        if (result.isSuccess) {
            repository.syncMergeToQueue(sessionId)
        }
        return result
    }
}
