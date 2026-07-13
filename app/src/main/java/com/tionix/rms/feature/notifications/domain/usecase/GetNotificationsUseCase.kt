package com.tionix.rms.feature.notifications.domain.usecase

import com.tionix.rms.feature.notifications.domain.model.Notification
import com.tionix.rms.feature.notifications.domain.repository.NotificationsRepository
import javax.inject.Inject

class GetNotificationsUseCase @Inject constructor(private val repository: NotificationsRepository) {
    suspend operator fun invoke(): Result<List<Notification>> {
        return repository.getNotifications()
    }
}

class MarkAsReadUseCase @Inject constructor(private val repository: NotificationsRepository) {
    suspend operator fun invoke(notificationId: String): Result<Unit> {
        return repository.markAsRead(notificationId)
    }
}

class MarkAllAsReadUseCase @Inject constructor(private val repository: NotificationsRepository) {
    suspend operator fun invoke(): Result<Unit> {
        return repository.markAllAsRead()
    }
}

class DeleteNotificationUseCase @Inject constructor(private val repository: NotificationsRepository) {
    suspend operator fun invoke(notificationId: String): Result<Unit> {
        return repository.deleteNotification(notificationId)
    }
}
