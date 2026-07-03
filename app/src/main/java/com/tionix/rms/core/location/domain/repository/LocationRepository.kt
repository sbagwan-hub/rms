package com.tionix.rms.core.location.domain.repository

import com.tionix.rms.core.location.domain.model.LocationData
import com.tionix.rms.core.location.domain.model.LocationTrackingState
import kotlinx.coroutines.flow.Flow

interface LocationRepository {
    val locationTrackingState: Flow<LocationTrackingState>
    
    suspend fun startTracking(): Result<Unit>
    suspend fun stopTracking(): Result<Unit>
    suspend fun getCurrentLocation(): Result<LocationData>
    suspend fun uploadLocation(location: LocationData): Result<Unit>
}
