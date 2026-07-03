package com.tionix.rms.feature.refile.data.remote

import com.tionix.rms.feature.refile.data.remote.dto.RefileDto
import com.tionix.rms.feature.refile.data.remote.dto.StartRefileRequestDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface RefileApiService {
    @GET("refiles/assigned")
    suspend fun getAssignedRefiles(): Response<List<RefileDto>>
    
    @POST("refiles/start")
    suspend fun startRefile(@Body request: StartRefileRequestDto): Response<RefileDto>
    
    @PUT("refiles/{id}/complete")
    suspend fun completeRefile(@Path("id") id: String): Response<Unit>
    
    @GET("refiles/scan/{barcode}")
    suspend fun scanFile(@Path("barcode") barcode: String): Response<RefileDto?>
}
