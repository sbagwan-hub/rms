package com.tionix.rms.feature.dashboard.data.remote

import com.tionix.rms.feature.dashboard.data.remote.dto.ReportsSummaryDto
import retrofit2.Response
import retrofit2.http.GET

interface DashboardReportsApiService {
    @GET("reports/summary")
    suspend fun getSummary(): Response<ReportsSummaryDto>
}
