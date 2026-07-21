package com.tionix.rms.core.network

import com.tionix.rms.core.network.dto.*
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * ApiService
 * ==========
 * The unified Retrofit api definition for all mobile-facing endpoints of the RMS system.
 * Response bodies are automatically unwrapped from `{ success: true, data: T }` envelopes
 * by [EnvelopeUnwrappingInterceptor] when requests are successful.
 */
interface ApiService {

    @POST("auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<LoginResponse>

    @GET("scan/lookup")
    suspend fun lookup(
        @Query("barcode") barcode: String
    ): Response<LookupData>

    @POST("scan")
    suspend fun recordScan(
        @Body request: ScanRequest
    ): Response<Unit>

    @POST("workflows/fresh-box")
    suspend fun submitFreshBox(
        @Body request: FreshBoxRequest
    ): Response<FreshBoxResponse>

    @POST("workflows/inventory")
    suspend fun submitInventory(
        @Body request: InventoryRequest
    ): Response<InventoryResponse>

    @GET("workflows/refile/target")
    suspend fun getRefileTarget(
        @Query("fileBarcode") fileBarcode: String
    ): Response<RefileTargetResponse>

    @POST("workflows/refile")
    suspend fun submitRefile(
        @Body request: RefileRequest
    ): Response<Unit>

    @GET("workflows/segregation/check")
    suspend fun checkSegregation(
        @Query("oldBoxBarcode") oldBoxBarcode: String,
        @Query("fileBarcode") fileBarcode: String
    ): Response<SegregationCheckResponse>

    @POST("workflows/segregation")
    suspend fun submitSegregation(
        @Body request: SegregationRequest
    ): Response<SegregationResponse>
    @GET("sites/public")
    suspend fun getSites(): Response<List<SiteDto>>
}
