package com.tionix.rms.core.location.domain.usecase

import com.tionix.rms.core.location.domain.repository.LocationRepository

class StartLocationTrackingUseCase(private val repository: LocationRepository) {
    suspend operator fun invoke(): Result<Unit> {
        return repository.startTracking()
    }
}

class StopLocationTrackingUseCase(private val repository: LocationRepository) {
    suspend operator fun invoke(): Result<Unit> {
        return repository.stopTracking()
    }
}

class GetCurrentLocationUseCase(private val repository: LocationRepository) {
    suspend operator fun invoke(): Result<com.tionix.rms.core.location.domain.model.LocationData> {
        return repository.getCurrentLocation()
    }
}
