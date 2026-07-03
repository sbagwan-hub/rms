package com.tionix.rms.feature.dashboard.data.remote

import com.tionix.rms.feature.dashboard.data.remote.dto.DashboardStatsDto
import com.tionix.rms.feature.dashboard.data.remote.dto.TaskDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface DashboardApiService {
    @GET("dashboard/stats")
    suspend fun getDashboardStats(): Response<DashboardStatsDto>
    
    @GET("dashboard/tasks")
    suspend fun getAssignedTasks(): Response<List<TaskDto>>
    
    @GET("dashboard/tasks")
    suspend fun getTasksByStatus(@Query("status") status: String): Response<List<TaskDto>>
}
