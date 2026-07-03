package com.tionix.rms.feature.freshboxmove.domain.usecase

import com.tionix.rms.feature.freshboxmove.domain.model.FreshBoxMove
import com.tionix.rms.feature.freshboxmove.domain.model.StartMoveRequest
import com.tionix.rms.feature.freshboxmove.domain.repository.FreshBoxMoveRepository

class StartMoveUseCase(private val repository: FreshBoxMoveRepository) {
    suspend operator fun invoke(request: StartMoveRequest): Result<FreshBoxMove> {
        return repository.startMove(request)
    }
}
