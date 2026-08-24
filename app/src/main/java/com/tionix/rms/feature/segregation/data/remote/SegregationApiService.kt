package com.tionix.rms.feature.segregation.data.remote

import com.google.gson.JsonObject
import com.tionix.rms.feature.segregation.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface SegregationApiService {
    @GET("segregations/assigned")
    suspend fun getAssignedSegregations(): Response<List<SegregationDto>>

    @GET("segregations/{id}")
    suspend fun getSegregationDetails(@Path("id") id: String): Response<SegregationDetailDataDto>

    @POST("segregations/{id}/validate-box")
    suspend fun validateBox(
        @Path("id") id: String,
        @Body request: ValidateBoxRequestDto
    ): Response<JsonObject>

    @POST("segregations/{id}/move-file")
    suspend fun moveFile(
        @Path("id") id: String,
        @Body request: MoveFileRequestDto
    ): Response<JsonObject>

    @POST("segregations/{id}/complete")
    suspend fun completeSegregation(@Path("id") id: String): Response<Unit>

    @PUT("segregations/{id}/complete")
    suspend fun completeSegregationPut(@Path("id") id: String): Response<Unit>

    @POST("segregations/start")
    suspend fun startSegregation(@Body request: StartSegregationRequestDto): Response<SegregationDto>

    @GET("segregations/scan/{barcode}")
    suspend fun scanBox(@Path("barcode") barcode: String): Response<SegregationDto?>
}
