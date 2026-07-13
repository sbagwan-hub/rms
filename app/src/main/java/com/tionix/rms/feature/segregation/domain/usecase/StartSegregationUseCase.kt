package com.tionix.rms.feature.segregation.domain.usecase

import com.tionix.rms.feature.segregation.domain.model.Segregation
import com.tionix.rms.feature.segregation.domain.model.StartSegregationRequest
import com.tionix.rms.feature.segregation.domain.repository.SegregationRepository
import javax.inject.Inject

class StartSegregationUseCase @Inject constructor(private val repository: SegregationRepository) {
    suspend operator fun invoke(request: StartSegregationRequest): Result<Segregation> {
        return repository.startSegregation(request)
    }
}
