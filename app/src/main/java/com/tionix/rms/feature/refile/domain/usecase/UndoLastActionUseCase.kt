package com.tionix.rms.feature.refile.domain.usecase

import com.tionix.rms.feature.refile.domain.repository.RefileRepository
import javax.inject.Inject

class UndoLastActionUseCase @Inject constructor(
    private val repository: RefileRepository
) {
    suspend operator fun invoke(sessionId: String): Result<Unit> {
        // TODO: BACKEND ENDPOINT PENDING
        return repository.undoLastAction(sessionId)
    }
}
