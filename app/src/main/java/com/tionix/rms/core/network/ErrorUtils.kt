package com.tionix.rms.core.network

import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

object ErrorUtils {
    /**
     * Maps technical exceptions to user-friendly messages, hiding details like IP addresses.
     */
    fun getFriendlyErrorMessage(throwable: Throwable): String {
        return when (throwable) {
            is ConnectException -> "Unable to connect to the server. Please check your network connection."
            is UnknownHostException -> "Server not found. Please check your internet connection."
            is SocketTimeoutException -> "Connection timed out. The server is taking too long to respond."
            else -> "A network error occurred. Please try again later."
        }
    }
}
