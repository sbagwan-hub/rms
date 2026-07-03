package com.tionix.rms.core.location.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tionix.rms.core.location.domain.repository.LocationRepository
import com.tionix.rms.core.location.domain.usecase.GetCurrentLocationUseCase
import com.tionix.rms.core.location.domain.usecase.StartLocationTrackingUseCase
import com.tionix.rms.core.location.domain.usecase.StopLocationTrackingUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LocationViewModel @Inject constructor(
    private val locationRepository: LocationRepository,
    private val startLocationTrackingUseCase: StartLocationTrackingUseCase,
    private val stopLocationTrackingUseCase: StopLocationTrackingUseCase,
    private val getCurrentLocationUseCase: GetCurrentLocationUseCase
) : ViewModel() {

    private val _locationState = MutableStateFlow<LocationUiState>(LocationUiState.Idle)
    val locationState: StateFlow<LocationUiState> = _locationState.asStateFlow()

    init {
        // Collect location tracking state
        viewModelScope.launch {
            locationRepository.locationTrackingState.collect { state ->
                _locationState.value = LocationUiState.Tracking(state)
            }
        }
    }

    fun startTracking() {
        viewModelScope.launch {
            _locationState.value = LocationUiState.Loading
            val result = startLocationTrackingUseCase()
            
            if (result.isSuccess) {
                _locationState.value = LocationUiState.TrackingStarted
            } else {
                _locationState.value = LocationUiState.Error(result.exceptionOrNull()?.message ?: "Failed to start tracking")
            }
        }
    }

    fun stopTracking() {
        viewModelScope.launch {
            val result = stopLocationTrackingUseCase()
            
            if (result.isSuccess) {
                _locationState.value = LocationUiState.TrackingStopped
            } else {
                _locationState.value = LocationUiState.Error(result.exceptionOrNull()?.message ?: "Failed to stop tracking")
            }
        }
    }

    fun getCurrentLocation() {
        viewModelScope.launch {
            _locationState.value = LocationUiState.Loading
            val result = getCurrentLocationUseCase()
            
            if (result.isSuccess) {
                _locationState.value = LocationUiState.LocationUpdated(result.getOrNull()!!)
            } else {
                _locationState.value = LocationUiState.Error(result.exceptionOrNull()?.message ?: "Failed to get location")
            }
        }
    }
}

sealed class LocationUiState {
    object Idle : LocationUiState()
    object Loading : LocationUiState()
    data class Tracking(val state: com.tionix.rms.core.location.domain.model.LocationTrackingState) : LocationUiState()
    object TrackingStarted : LocationUiState()
    object TrackingStopped : LocationUiState()
    data class LocationUpdated(val location: com.tionix.rms.core.location.domain.model.LocationData) : LocationUiState()
    data class Error(val message: String) : LocationUiState()
}
