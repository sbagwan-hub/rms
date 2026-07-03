package com.tionix.rms.feature.reports.data.remote

import com.tionix.rms.feature.reports.data.remote.dto.ActivityHistoryDto
import com.tionix.rms.feature.reports.data.remote.dto.ReportDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ReportsApiService {
    @GET("reports")
    suspend fun getReports(@Query("type") type: String?): Response<List<ReportDto>>
    
    @GET("reports/activity-history")
    suspend fun getActivityHistory(@Query("limit") limit: Int): Response<List<ActivityHistoryDto>>
    
    @GET("reports/{id}/download")
    suspend fun downloadReport(@Path("id") id: String): Response<String>
}
