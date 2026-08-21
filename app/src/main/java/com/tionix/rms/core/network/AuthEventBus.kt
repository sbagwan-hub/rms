package com.tionix.rms.core.network

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

sealed class AuthEvent {
    data class SessionExpired(val reason: String = "Session expired. Please log in again.") : AuthEvent()
    object LoggedOut : AuthEvent()
}

@Singleton
class AuthEventBus @Inject constructor() {
    private val _events = MutableSharedFlow<AuthEvent>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val events = _events.asSharedFlow()

    fun emitLogout(reason: String = "Session expired. Please log in again.") {
        _events.tryEmit(AuthEvent.SessionExpired(reason))
    }

    fun emitUserLoggedOut() {
        _events.tryEmit(AuthEvent.LoggedOut)
    }
}
