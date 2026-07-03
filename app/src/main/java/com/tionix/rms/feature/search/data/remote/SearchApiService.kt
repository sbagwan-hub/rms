package com.tionix.rms.feature.search.data.remote

import com.tionix.rms.feature.search.data.remote.dto.SearchResultDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface SearchApiService {
    @GET("search")
    suspend fun search(
        @Query("query") query: String,
        @Query("type") type: String
    ): Response<List<SearchResultDto>>
    
    @GET("search/barcode")
    suspend fun searchByBarcode(@Query("barcode") barcode: String): Response<SearchResultDto?>
}
