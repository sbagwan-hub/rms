package com.tionix.rms.feature.notifications.data.repository

import com.tionix.rms.feature.notifications.data.remote.NotificationsApiService
import com.tionix.rms.feature.notifications.domain.model.Notification
import com.tionix.rms.feature.notifications.domain.repository.NotificationsRepository
import javax.inject.Inject

class NotificationsRepositoryImpl @Inject constructor(
    private val apiService: NotificationsApiService
) : NotificationsRepository {

    override suspend fun getNotifications(): Result<List<Notification>> {
        return try {
            val response = apiService.getNotifications()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.map { it.toDomain() })
            } else {
                Result.failure(Exception("Failed to fetch notifications"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun markAsRead(notificationId: String): Result<Unit> {
        return try {
            val response = apiService.markAsRead(notificationId)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to mark as read"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun markAllAsRead(): Result<Unit> {
        return try {
            val response = apiService.markAllAsRead()
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to mark all as read"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteNotification(notificationId: String): Result<Unit> {
        return try {
            val response = apiService.deleteNotification(notificationId)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to delete notification"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
