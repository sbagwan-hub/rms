package com.tionix.rms.feature.notifications.data.remote.dto

data class NotificationDto(
    val id: String,
    val type: String,
    val title: String,
    val message: String,
    val isRead: Boolean,
    val priority: String,
    val createdAt: String,
    val actionUrl: String?
)
