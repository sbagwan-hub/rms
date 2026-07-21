package com.tionix.rms.core.network

import com.tionix.rms.feature.auth.data.local.AuthPreferences
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

/**
 * AuthInterceptor
 * ===============
 * Appends the standard "Authorization: Bearer <token>" header to outgoing network requests
 * if an access token is found in the local [AuthPreferences] DataStore.
 */
@Singleton
class AuthInterceptor @Inject constructor(
    private val authPreferences: AuthPreferences
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val token = runBlocking { authPreferences.getAccessToken() }
        val originalRequest = chain.request()

        return if (token.isNullOrBlank()) {
            chain.proceed(originalRequest)
        } else {
            val authenticatedRequest = originalRequest.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
            chain.proceed(authenticatedRequest)
        }
    }
}
