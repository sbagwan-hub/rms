package com.tionix.rms.feature.refile.domain.usecase

import com.tionix.rms.feature.refile.domain.model.Refile
import com.tionix.rms.feature.refile.domain.model.StartRefileRequest
import com.tionix.rms.feature.refile.domain.repository.RefileRepository

class StartRefileUseCase(private val repository: RefileRepository) {
    suspend operator fun invoke(request: StartRefileRequest): Result<Refile> {
        return repository.startRefile(request)
    }
}
