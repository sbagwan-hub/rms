package com.tionix.rms.core.location.data.repository

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import androidx.core.content.ContextCompat
import com.tionix.rms.core.location.domain.model.LocationData
import com.tionix.rms.core.location.domain.model.LocationTrackingState
import com.tionix.rms.core.location.domain.repository.LocationRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocationRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : LocationRepository {

    private val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    private var isTracking = false
    private var currentLocation: LocationData? = null
    private var lastUpdateTime: Long? = null

    override val locationTrackingState: Flow<LocationTrackingState>
        get() = callbackFlow {
            trySend(LocationTrackingState(isTracking, currentLocation, lastUpdateTime))
            awaitClose()
        }

    @SuppressLint("MissingPermission")
    override suspend fun startTracking(): Result<Unit> {
        return try {
            if (!hasLocationPermission()) {
                return Result.failure(SecurityException("Location permission not granted"))
            }

            if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) &&
                !locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                return Result.failure(Exception("Location services are disabled"))
            }

            isTracking = true
            
            val locationListener = object : LocationListener {
                override fun onLocationChanged(location: Location) {
                    currentLocation = LocationData(
                        latitude = location.latitude,
                        longitude = location.longitude,
                        accuracy = location.accuracy,
                        timestamp = System.currentTimeMillis()
                    )
                    lastUpdateTime = System.currentTimeMillis()
                }

                override fun onProviderEnabled(provider: String) {}
                override fun onProviderDisabled(provider: String) {}
            }

            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                10000L,
                10f,
                locationListener
            )

            locationManager.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER,
                10000L,
                10f,
                locationListener
            )

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun stopTracking(): Result<Unit> {
        return try {
            isTracking = false
            // In production, would remove location updates here
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    @SuppressLint("MissingPermission")
    override suspend fun getCurrentLocation(): Result<LocationData> {
        return try {
            if (!hasLocationPermission()) {
                return Result.failure(SecurityException("Location permission not granted"))
            }

            val location = when {
                locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) -> {
                    locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                }
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) -> {
                    locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                }
                else -> null
            }

            if (location != null) {
                Result.success(
                    LocationData(
                        latitude = location.latitude,
                        longitude = location.longitude,
                        accuracy = location.accuracy,
                        timestamp = location.time
                    )
                )
            } else {
                Result.failure(Exception("Unable to get current location"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun uploadLocation(location: LocationData): Result<Unit> {
        // In production, this would send the location to the backend API
        // For now, return success
        return Result.success(Unit)
    }

    private fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }
}
