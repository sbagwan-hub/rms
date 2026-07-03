package com.tionix.rms.feature.merge.data.remote

import com.tionix.rms.feature.merge.data.remote.dto.MergeDto
import com.tionix.rms.feature.merge.data.remote.dto.StartMergeRequestDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface MergeApiService {
    @GET("merges/assigned")
    suspend fun getAssignedMerges(): Response<List<MergeDto>>
    
    @POST("merges/start")
    suspend fun startMerge(@Body request: StartMergeRequestDto): Response<MergeDto>
    
    @PUT("merges/{id}/complete")
    suspend fun completeMerge(@Path("id") id: String): Response<Unit>
    
    @GET("merges/scan/{barcode}")
    suspend fun scanBox(@Path("barcode") barcode: String): Response<MergeDto?>
}
