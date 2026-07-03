package com.tionix.rms.feature.inventory.data.remote

import com.tionix.rms.feature.inventory.data.remote.dto.InventoryVerificationDto
import com.tionix.rms.feature.inventory.data.remote.dto.StartVerificationRequestDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface InventoryApiService {
    @GET("inventory-verifications/assigned")
    suspend fun getAssignedVerifications(): Response<List<InventoryVerificationDto>>
    
    @POST("inventory-verifications/start")
    suspend fun startVerification(@Body request: StartVerificationRequestDto): Response<InventoryVerificationDto>
    
    @PUT("inventory-verifications/{id}/complete")
    suspend fun completeVerification(@Path("id") id: String): Response<Unit>
    
    @POST("inventory-verifications/{id}/scan")
    suspend fun scanBox(@Path("id") id: String, @Body barcode: String): Response<Unit>
}
