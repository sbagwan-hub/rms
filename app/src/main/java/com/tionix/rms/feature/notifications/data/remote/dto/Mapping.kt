package com.tionix.rms.feature.notifications.data.remote.dto

import com.tionix.rms.feature.notifications.domain.model.Notification
import com.tionix.rms.feature.notifications.domain.model.NotificationPriority
import com.tionix.rms.feature.notifications.domain.model.NotificationType

fun NotificationDto.toDomain(): Notification {
    return Notification(
        id = id,
        type = NotificationType.valueOf(type),
        title = title,
        message = message,
        isRead = isRead,
        priority = NotificationPriority.valueOf(priority),
        createdAt = createdAt,
        actionUrl = actionUrl
    )
}
