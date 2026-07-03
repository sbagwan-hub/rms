package com.tionix.rms.feature.transfer.data.remote

import com.tionix.rms.feature.transfer.data.remote.dto.StartTransferRequestDto
import com.tionix.rms.feature.transfer.data.remote.dto.TransferDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface TransferApiService {
    @GET("transfers/assigned")
    suspend fun getAssignedTransfers(): Response<List<TransferDto>>
    
    @POST("transfers/start")
    suspend fun startTransfer(@Body request: StartTransferRequestDto): Response<TransferDto>
    
    @PUT("transfers/{id}/complete")
    suspend fun completeTransfer(@Path("id") id: String): Response<Unit>
    
    @GET("transfers/scan/{barcode}")
    suspend fun scanBox(@Path("barcode") barcode: String): Response<TransferDto?>
}
