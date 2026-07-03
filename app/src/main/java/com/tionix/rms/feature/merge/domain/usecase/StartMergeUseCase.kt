package com.tionix.rms.feature.merge.domain.usecase

import com.tionix.rms.feature.merge.domain.model.Merge
import com.tionix.rms.feature.merge.domain.model.StartMergeRequest
import com.tionix.rms.feature.merge.domain.repository.MergeRepository

class StartMergeUseCase(private val repository: MergeRepository) {
    suspend operator fun invoke(request: StartMergeRequest): Result<Merge> {
        return repository.startMerge(request)
    }
}
