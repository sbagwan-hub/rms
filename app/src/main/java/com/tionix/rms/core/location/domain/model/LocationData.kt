package com.tionix.rms.core.location.domain.model

data class LocationData(
    val latitude: Double,
    val longitude: Double,
    val accuracy: Float,
    val timestamp: Long
)

data class LocationTrackingState(
    val isTracking: Boolean,
    val currentLocation: LocationData?,
    val lastUpdateTime: Long?
)
