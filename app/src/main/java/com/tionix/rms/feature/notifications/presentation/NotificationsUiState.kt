package com.tionix.rms.feature.notifications.presentation

import com.tionix.rms.feature.notifications.domain.model.Notification

sealed class NotificationsUiState {
    object Loading : NotificationsUiState()
    data class Success(val notifications: List<Notification>) : NotificationsUiState()
    data class Error(val message: String) : NotificationsUiState()
    object MarkedAsRead : NotificationsUiState()
    object Deleted : NotificationsUiState()
}
