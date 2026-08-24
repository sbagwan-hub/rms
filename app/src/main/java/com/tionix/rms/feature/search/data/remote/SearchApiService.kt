package com.tionix.rms.feature.search.data.remote

import com.tionix.rms.feature.search.data.remote.dto.BoxDetailDto
import com.tionix.rms.feature.search.data.remote.dto.SearchResultDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface SearchApiService {
    @GET("search")
    suspend fun search(
        @Query("query") query: String,
        @Query("type") type: String
    ): Response<List<SearchResultDto>>
    
    @GET("search/barcode")
    suspend fun searchByBarcode(@Query("barcode") barcode: String): Response<SearchResultDto?>

    @GET("search/boxes/{id}")
    suspend fun getBoxDetail(@Path("id") boxId: String): Response<BoxDetailDto>

    @GET("search/files/{id}")
    suspend fun getFileDetail(@Path("id") fileId: String): Response<com.tionix.rms.feature.filesearch.data.remote.dto.FileDetailDto>

    @retrofit2.http.POST("search/boxes/{id}/files")
    suspend fun insertFile(
        @Path("id") boxId: String,
        @retrofit2.http.Body request: InsertFileRequest
    ): Response<InsertFileResponse>

    @retrofit2.http.POST("search/refile")
    suspend fun refileFile(
        @retrofit2.http.Body request: RefileRequest
    ): Response<RefileResponse>
}

data class RefileRequest(
    val fileBarcode: String,
    val targetBoxBarcode: String
)

data class RefileResponse(
    val success: Boolean = true,
    val message: String? = null,
    val data: RefileData? = null
)

data class RefileData(
    val fileId: String? = null,
    val fileBarcode: String? = null,
    val sourceBoxId: String? = null,
    val sourceBoxBarcode: String? = null,
    val targetBoxId: String? = null,
    val targetBoxBarcode: String? = null,
    val previousBoxId: String? = null,
    val previousBoxBarcode: String? = null,
    val newBoxId: String? = null,
    val newBoxBarcode: String? = null,
    val previousLocation: String? = null,
    val newLocation: String? = null
)

data class InsertFileRequest(
    val fileBarcode: String,
    val title: String? = null
)

data class InsertFileResponse(
    val success: Boolean = true,
    val message: String? = null,
    val data: InsertFileData? = null,
    val id: String? = null,
    val barcode: String? = null,
    val boxId: String? = null,
    val boxBarcode: String? = null
)

data class InsertFileData(
    val id: String,
    val barcode: String,
    val title: String,
    val boxId: String,
    val boxBarcode: String
)
