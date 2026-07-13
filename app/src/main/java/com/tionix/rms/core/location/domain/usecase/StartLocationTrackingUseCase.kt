package com.tionix.rms.core.location.domain.usecase

import com.tionix.rms.core.location.domain.repository.LocationRepository
import javax.inject.Inject

class StartLocationTrackingUseCase @Inject constructor(private val repository: LocationRepository) {
    suspend operator fun invoke(): Result<Unit> {
        return repository.startTracking()
    }
}

class StopLocationTrackingUseCase @Inject constructor(private val repository: LocationRepository) {
    suspend operator fun invoke(): Result<Unit> {
        return repository.stopTracking()
    }
}

class GetCurrentLocationUseCase @Inject constructor(private val repository: LocationRepository) {
    suspend operator fun invoke(): Result<com.tionix.rms.core.location.domain.model.LocationData> {
        return repository.getCurrentLocation()
    }
}
