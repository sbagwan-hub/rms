package com.tionix.rms.feature.notifications.domain.model

data class Notification(
    val id: String,
    val type: NotificationType,
    val title: String,
    val message: String,
    val isRead: Boolean,
    val priority: NotificationPriority,
    val createdAt: String,
    val actionUrl: String?
)

enum class NotificationType {
    TASK_ASSIGNED,
    TASK_COMPLETED,
    TASK_FAILED,
    SYSTEM,
    INFO
}

enum class NotificationPriority {
    LOW,
    MEDIUM,
    HIGH,
    URGENT
}
