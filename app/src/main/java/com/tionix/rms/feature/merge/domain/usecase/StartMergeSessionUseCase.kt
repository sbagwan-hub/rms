package com.tionix.rms.feature.merge.domain.usecase

import com.tionix.rms.feature.merge.domain.model.MergeSession
import com.tionix.rms.feature.merge.domain.repository.MergeRepository
import javax.inject.Inject

class StartMergeSessionUseCase @Inject constructor(
    private val repository: MergeRepository
) {
    suspend operator fun invoke(): Result<MergeSession> {
        // TODO: BACKEND ENDPOINT PENDING
        return repository.startMergeSession()
    }
}
