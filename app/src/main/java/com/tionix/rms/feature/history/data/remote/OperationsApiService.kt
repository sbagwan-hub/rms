package com.tionix.rms.feature.history.data.remote

import com.tionix.rms.feature.history.data.remote.dto.OperationSummaryDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface OperationsApiService {
    @GET("operations")
    suspend fun listOperations(
        @Query("mine") mine: Boolean = true,
        @Query("limit") limit: Int = 50,
        @Query("page") page: Int = 1
    ): Response<List<OperationSummaryDto>>
}
