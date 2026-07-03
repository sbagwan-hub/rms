package com.tionix.rms.feature.notifications.domain.usecase

import com.tionix.rms.feature.notifications.domain.model.Notification
import com.tionix.rms.feature.notifications.domain.repository.NotificationsRepository

class GetNotificationsUseCase(private val repository: NotificationsRepository) {
    suspend operator fun invoke(): Result<List<Notification>> {
        return repository.getNotifications()
    }
}

class MarkAsReadUseCase(private val repository: NotificationsRepository) {
    suspend operator fun invoke(notificationId: String): Result<Unit> {
        return repository.markAsRead(notificationId)
    }
}

class MarkAllAsReadUseCase(private val repository: NotificationsRepository) {
    suspend operator fun invoke(): Result<Unit> {
        return repository.markAllAsRead()
    }
}

class DeleteNotificationUseCase(private val repository: NotificationsRepository) {
    suspend operator fun invoke(notificationId: String): Result<Unit> {
        return repository.deleteNotification(notificationId)
    }
}
