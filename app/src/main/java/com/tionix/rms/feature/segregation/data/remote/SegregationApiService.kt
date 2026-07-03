package com.tionix.rms.feature.segregation.data.remote

import com.tionix.rms.feature.segregation.data.remote.dto.SegregationDto
import com.tionix.rms.feature.segregation.data.remote.dto.StartSegregationRequestDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface SegregationApiService {
    @GET("segregations/assigned")
    suspend fun getAssignedSegregations(): Response<List<SegregationDto>>
    
    @POST("segregations/start")
    suspend fun startSegregation(@Body request: StartSegregationRequestDto): Response<SegregationDto>
    
    @PUT("segregations/{id}/complete")
    suspend fun completeSegregation(@Path("id") id: String): Response<Unit>
    
    @GET("segregations/scan/{barcode}")
    suspend fun scanBox(@Path("barcode") barcode: String): Response<SegregationDto?>
}
