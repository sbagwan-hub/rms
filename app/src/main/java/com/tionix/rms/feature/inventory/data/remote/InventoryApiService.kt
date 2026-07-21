package com.tionix.rms.feature.inventory.data.remote

import com.tionix.rms.feature.inventory.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.*

interface InventoryApiService {
    @GET("workflows/inventory-verify/sessions")
    suspend fun getAssignedVerifications(): Response<List<InventoryVerificationDto>>
    
    @POST("workflows/inventory-verify/sessions")
    suspend fun startVerification(@Body request: StartVerificationRequestDto): Response<InventoryVerificationDto>
    
    @PUT("workflows/inventory-verify/sessions/{id}/end")
    suspend fun completeVerification(@Path("id") id: String): Response<InventoryVerificationDto>
    
    @POST("workflows/inventory-verify/sessions/{id}/scans")
    suspend fun scanBox(
        @Path("id") id: String,
        @Body request: SubmitVerifyScanRequestDto
    ): Response<InventoryVerificationScanDto>

    @GET("boxes/barcode/{barcode}")
    suspend fun resolveBoxBarcode(@Path("barcode") barcode: String): Response<BoxDetailsDto>

    @GET("boxes/{boxId}/files")
    suspend fun getFilesByBox(@Path("boxId") boxId: String): Response<List<FileRecordDto>>
}
