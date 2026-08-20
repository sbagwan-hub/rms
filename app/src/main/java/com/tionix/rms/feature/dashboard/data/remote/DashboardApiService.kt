package com.tionix.rms.feature.dashboard.data.remote

import com.tionix.rms.feature.dashboard.data.remote.dto.DashboardStatsDto
import com.tionix.rms.feature.dashboard.data.remote.dto.TaskDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface DashboardApiService {
    @GET("dashboard/stats")
    suspend fun getDashboardStats(): Response<DashboardStatsDto>
    
    @GET("dashboard/tasks")
    suspend fun getAssignedTasks(): Response<List<TaskDto>>
    
    @GET("dashboard/tasks")
    suspend fun getTasksByStatus(@Query("status") status: String): Response<List<TaskDto>>

    @POST("tasks/{id}/accept")
    suspend fun acceptTask(@Path("id") taskId: String): Response<Map<String, Any>>

    @POST("tasks/{id}/start")
    suspend fun startTask(@Path("id") taskId: String): Response<Map<String, Any>>

    @POST("tasks/{id}/complete")
    suspend fun completeTask(
        @Path("id") taskId: String,
        @Body payload: Map<String, String> = emptyMap()
    ): Response<Map<String, Any>>

    @POST("tasks/{id}/reject")
    suspend fun rejectTask(
        @Path("id") taskId: String,
        @Body payload: Map<String, String> = emptyMap()
    ): Response<Map<String, Any>>
}
