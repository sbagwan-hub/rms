package com.tionix.rms.feature.sync.data.remote

import com.tionix.rms.feature.sync.data.remote.dto.SyncOperationResultDto
import com.tionix.rms.feature.sync.data.remote.dto.SyncOperationsRequestDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface SyncApiService {
    @POST("sync/operations")
    suspend fun syncOperations(
        @Body request: SyncOperationsRequestDto
    ): Response<List<SyncOperationResultDto>>
}
