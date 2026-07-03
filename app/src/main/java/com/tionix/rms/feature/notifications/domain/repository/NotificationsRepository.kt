package com.tionix.rms.feature.notifications.domain.repository

import com.tionix.rms.feature.notifications.domain.model.Notification

interface NotificationsRepository {
    suspend fun getNotifications(): Result<List<Notification>>
    suspend fun markAsRead(notificationId: String): Result<Unit>
    suspend fun markAllAsRead(): Result<Unit>
    suspend fun deleteNotification(notificationId: String): Result<Unit>
}
