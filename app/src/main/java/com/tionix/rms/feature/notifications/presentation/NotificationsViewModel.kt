package com.tionix.rms.feature.notifications.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tionix.rms.feature.notifications.domain.usecase.DeleteNotificationUseCase
import com.tionix.rms.feature.notifications.domain.usecase.GetNotificationsUseCase
import com.tionix.rms.feature.notifications.domain.usecase.MarkAllAsReadUseCase
import com.tionix.rms.feature.notifications.domain.usecase.MarkAsReadUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotificationsViewModel @Inject constructor(
    private val getNotificationsUseCase: GetNotificationsUseCase,
    private val markAsReadUseCase: MarkAsReadUseCase,
    private val markAllAsReadUseCase: MarkAllAsReadUseCase,
    private val deleteNotificationUseCase: DeleteNotificationUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<NotificationsUiState>(NotificationsUiState.Loading)
    val uiState: StateFlow<NotificationsUiState> = _uiState.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _refreshError = kotlinx.coroutines.flow.MutableSharedFlow<String>()
    val refreshError: kotlinx.coroutines.flow.SharedFlow<String> = _refreshError.asSharedFlow()

    init {
        loadNotifications()
    }

    fun loadNotifications(isRefresh: Boolean = false) {
        viewModelScope.launch {
            if (isRefresh) {
                if (_isRefreshing.value) return@launch
                _isRefreshing.value = true
                android.util.Log.d("APPBAR_REFRESH", "Screen: Notifications\nAPI request started")
            } else {
                _uiState.value = NotificationsUiState.Loading
            }

            val result = getNotificationsUseCase()
            
            if (result.isSuccess) {
                android.util.Log.d("APPBAR_REFRESH", "Screen: Notifications\nAPI response: 200\nState updated")
                _uiState.value = NotificationsUiState.Success(result.getOrNull() ?: emptyList())
            } else {
                if (isRefresh && _uiState.value is NotificationsUiState.Success) {
                    _refreshError.emit("Unable to refresh data. Please try again.")
                } else {
                    _uiState.value = NotificationsUiState.Error(
                        result.exceptionOrNull()?.message ?: "Failed to load notifications"
                    )
                }
            }
            _isRefreshing.value = false
            if (isRefresh) {
                android.util.Log.d("APPBAR_REFRESH", "Screen: Notifications\nRefresh completed")
            }
        }
    }

    fun markAsRead(notificationId: String) {
        viewModelScope.launch {
            val result = markAsReadUseCase(notificationId)
            if (result.isSuccess) {
                _uiState.value = NotificationsUiState.MarkedAsRead
                loadNotifications()
            } else {
                _uiState.value = NotificationsUiState.Error(result.exceptionOrNull()?.message ?: "Failed to mark as read")
            }
        }
    }

    fun markAllAsRead() {
        viewModelScope.launch {
            val result = markAllAsReadUseCase()
            if (result.isSuccess) {
                _uiState.value = NotificationsUiState.MarkedAsRead
                loadNotifications()
            } else {
                _uiState.value = NotificationsUiState.Error(result.exceptionOrNull()?.message ?: "Failed to mark all as read")
            }
        }
    }

    fun deleteNotification(notificationId: String) {
        viewModelScope.launch {
            val result = deleteNotificationUseCase(notificationId)
            if (result.isSuccess) {
                _uiState.value = NotificationsUiState.Deleted
                loadNotifications()
            } else {
                _uiState.value = NotificationsUiState.Error(result.exceptionOrNull()?.message ?: "Failed to delete notification")
            }
        }
    }
}
