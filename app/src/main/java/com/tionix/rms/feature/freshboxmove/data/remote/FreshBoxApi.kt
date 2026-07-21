package com.tionix.rms.feature.freshboxmove.data.remote

import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface FreshBoxApi {
    @POST("workflows/fresh-box-move/sessions")
    suspend fun startSession(@Body request: StartSessionRequestDto): Response<FreshBoxMoveSessionDto>
    
    @POST("workflows/fresh-box-move/sessions/{sessionId}/scans")
    suspend fun submitScan(
        @Path("sessionId") sessionId: String,
        @Body request: SubmitScanRequestDto
    ): Response<FreshBoxMoveScanDto>
    
    @PUT("workflows/fresh-box-move/sessions/{sessionId}/end")
    suspend fun endSession(@Path("sessionId") sessionId: String): Response<FreshBoxMoveSessionDto>
    
    @GET("workflows/fresh-box-move/sessions/{sessionId}")
    suspend fun getSessionDetails(@Path("sessionId") sessionId: String): Response<FreshBoxMoveSessionDto>
}

data class StartSessionRequestDto(
    val deviceId: String?
)

data class FreshBoxMoveSessionDto(
    val id: String,
    val operatorId: String,
    val startedAt: String,
    val endedAt: String?,
    val deviceId: String?,
    val scans: List<FreshBoxMoveScanDto>? = emptyList()
)

data class SubmitScanRequestDto(
    val locationBarcode: String,
    val boxBarcode: String,
    // Wire contract is "clientOpId" (backend's submitScanSchema); kept as
    // clientEventId locally to match the Room entity / idempotency-key naming.
    @SerializedName("clientOpId") val clientEventId: String,
    @SerializedName("latitude") val gpsLat: Double?,
    @SerializedName("longitude") val gpsLng: Double?,
    val scannedAt: String
)

data class FreshBoxMoveScanDto(
    val id: String,
    val sessionId: String,
    val locationId: String,
    val boxId: String,
    val clientEventId: String,
    val gpsLat: Double?,
    val gpsLng: Double?,
    val scannedAt: String,
    val box: BoxDetailsDto,
    val location: LocationDetailsDto
)

data class BoxDetailsDto(
    val id: String,
    val barcode: String,
    val description: String?
)

data class LocationDetailsDto(
    val id: String,
    val barcode: String,
    val name: String?
)
