package com.tionix.rms.feature.freshboxmove.data.remote

import com.tionix.rms.feature.freshboxmove.data.remote.dto.FreshBoxMoveDto
import com.tionix.rms.feature.freshboxmove.data.remote.dto.StartMoveRequestDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface FreshBoxMoveApiService {
    @GET("fresh-box-moves/assigned")
    suspend fun getAssignedMoves(): Response<List<FreshBoxMoveDto>>
    
    @POST("fresh-box-moves/start")
    suspend fun startMove(@Body request: StartMoveRequestDto): Response<FreshBoxMoveDto>
    
    @PUT("fresh-box-moves/{id}/complete")
    suspend fun completeMove(@Path("id") id: String): Response<Unit>
    
    @GET("fresh-box-moves/scan/{barcode}")
    suspend fun scanBox(@Path("barcode") barcode: String): Response<FreshBoxMoveDto?>
}
