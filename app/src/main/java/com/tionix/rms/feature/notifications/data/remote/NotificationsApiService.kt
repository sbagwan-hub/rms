package com.tionix.rms.feature.notifications.data.remote

import com.tionix.rms.feature.notifications.data.remote.dto.NotificationDto
import retrofit2.Response
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path

interface NotificationsApiService {
    @GET("notifications")
    suspend fun getNotifications(): Response<List<NotificationDto>>
    
    @PUT("notifications/{id}/read")
    suspend fun markAsRead(@Path("id") id: String): Response<Unit>
    
    @PUT("notifications/read-all")
    suspend fun markAllAsRead(): Response<Unit>
    
    @DELETE("notifications/{id}")
    suspend fun deleteNotification(@Path("id") id: String): Response<Unit>
}
