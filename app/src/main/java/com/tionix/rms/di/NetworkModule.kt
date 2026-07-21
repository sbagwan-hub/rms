package com.tionix.rms.di

import com.tionix.rms.core.network.ApiService
import com.tionix.rms.feature.auth.data.local.AuthPreferences
import com.tionix.rms.feature.auth.data.remote.AuthApiService
import com.tionix.rms.feature.dashboard.data.remote.DashboardApiService
import com.tionix.rms.feature.inventory.data.remote.InventoryApiService
import com.tionix.rms.feature.merge.data.remote.MergeApiService
import com.tionix.rms.feature.notifications.data.remote.NotificationsApiService
import com.tionix.rms.feature.refile.data.remote.RefileApiService
import com.tionix.rms.feature.reports.data.remote.ReportsApiService
import com.tionix.rms.feature.search.data.remote.SearchApiService
import com.tionix.rms.feature.segregation.data.remote.SegregationApiService
import com.tionix.rms.feature.transfer.data.remote.TransferApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.Authenticator
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.Route
import okhttp3.ResponseBody.Companion.toResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.google.gson.JsonParser
import kotlinx.coroutines.runBlocking
import javax.inject.Singleton

/**
 * Base URL for the mobile-facing backend API surface (`/api/v1/mobile/`).
 * The admin-only surface (`/api/v1/admin/`) is out of scope for this app.
 * 10.0.2.2 is the Android emulator's alias for the host machine's localhost.
 */
private const val BASE_URL = "http://192.168.1.4:3001/api/v1/mobile/"

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideAuthInterceptor(preferences: AuthPreferences): Interceptor =
        Interceptor { chain ->
            val token = kotlinx.coroutines.runBlocking {
                preferences.getAccessToken()
            }
            val request = if (token.isNullOrBlank()) {
                chain.request()
            } else {
                chain.request().newBuilder()
                    .addHeader("Authorization", "Bearer $token")
                    .build()
            }
            chain.proceed(request)
        }

    @Provides
    @Singleton
    fun provideTokenAuthenticator(preferences: AuthPreferences): Authenticator =
        TokenAuthenticator(preferences)

    @Provides
    @Singleton
    fun provideOkHttpClient(authInterceptor: Interceptor, tokenAuthenticator: Authenticator): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }

        return OkHttpClient.Builder()
            .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .writeTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .addInterceptor(authInterceptor)
            .addInterceptor(EnvelopeUnwrappingInterceptor())
            .addInterceptor(logging)
            // Access tokens expire after 15 minutes (backend ACCESS_TOKEN_EXPIRY).
            // Without this, every request made after the token expires fails with
            // a bare 401 and screens show a generic "Failed to fetch..." error
            // until the user manually logs out and back in.
            .authenticator(tokenAuthenticator)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    @Provides
    @Singleton
    fun provideAuthApiService(retrofit: Retrofit): AuthApiService =
        retrofit.create(AuthApiService::class.java)

    @Provides
    @Singleton
    fun provideDashboardApiService(retrofit: Retrofit): DashboardApiService =
        retrofit.create(DashboardApiService::class.java)

    @Provides
    @Singleton
    fun provideFreshBoxApi(retrofit: Retrofit): com.tionix.rms.feature.freshboxmove.data.remote.FreshBoxApi =
        retrofit.create(com.tionix.rms.feature.freshboxmove.data.remote.FreshBoxApi::class.java)

    @Provides
    @Singleton
    fun provideInventoryApiService(retrofit: Retrofit): InventoryApiService =
        retrofit.create(InventoryApiService::class.java)

    @Provides
    @Singleton
    fun provideMergeApiService(retrofit: Retrofit): MergeApiService =
        retrofit.create(MergeApiService::class.java)

    @Provides
    @Singleton
    fun provideNotificationsApiService(retrofit: Retrofit): NotificationsApiService =
        retrofit.create(NotificationsApiService::class.java)

    @Provides
    @Singleton
    fun provideRefileApiService(retrofit: Retrofit): RefileApiService =
        retrofit.create(RefileApiService::class.java)

    @Provides
    @Singleton
    fun provideReportsApiService(retrofit: Retrofit): ReportsApiService =
        retrofit.create(ReportsApiService::class.java)

    @Provides
    @Singleton
    fun provideSearchApiService(retrofit: Retrofit): SearchApiService =
        retrofit.create(SearchApiService::class.java)

    @Provides
    @Singleton
    fun provideSegregationApiService(retrofit: Retrofit): SegregationApiService =
        retrofit.create(SegregationApiService::class.java)

    @Provides
    @Singleton
    fun provideTransferApiService(retrofit: Retrofit): TransferApiService =
        retrofit.create(TransferApiService::class.java)

    @Provides
    @Singleton
    fun provideApiService(retrofit: Retrofit): ApiService =
        retrofit.create(ApiService::class.java)
}

/**
 * On a 401 response, calls POST auth/refresh with the stored refresh token and
 * retries the original request with the new access token. Runs on OkHttp's own
 * dispatcher thread (never the main thread), so blocking DataStore reads/writes
 * via [runBlocking] are safe here — this is OkHttp's documented pattern for
 * Authenticators backed by suspend-based token storage.
 *
 * Uses a bare OkHttpClient (no auth interceptor) for the refresh call itself,
 * to avoid recursing back into this same authenticator.
 */
class TokenAuthenticator(
    private val preferences: AuthPreferences,
) : Authenticator {

    private val refreshClient = OkHttpClient.Builder()
        .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
        .build()

    override fun authenticate(route: Route?, response: Response): Request? {
        val path = response.request.url.encodedPath
        if (path.endsWith("auth/login") || path.endsWith("auth/refresh")) {
            // A 401 here means bad credentials or a dead refresh token, not an
            // expired access token — retrying would just mask the real error.
            return null
        }
        if (responseCount(response) >= 2) {
            return null // already retried once for this request — give up
        }

        val refreshToken = runBlocking { preferences.getRefreshToken() }
        if (refreshToken.isNullOrBlank()) return null

        val newAccessToken = runBlocking { callRefresh(refreshToken) }
        if (newAccessToken == null) {
            // Refresh token itself is dead — clear the session so the next
            // screen load's session check routes back to Login (07-navigation.md).
            runBlocking { preferences.clear() }
            return null
        }

        return response.request.newBuilder()
            .header("Authorization", "Bearer $newAccessToken")
            .build()
    }

    private suspend fun callRefresh(refreshToken: String): String? {
        val requestBody = """{"refreshToken":"$refreshToken"}"""
            .toRequestBody("application/json; charset=utf-8".toMediaType())
        val request = Request.Builder()
            .url(BASE_URL + "auth/refresh")
            .post(requestBody)
            .build()

        return try {
            refreshClient.newCall(request).execute().use { resp ->
                if (!resp.isSuccessful) return null
                val bodyStr = resp.body?.string() ?: return null
                val json = JsonParser.parseString(bodyStr).asJsonObject
                val data = if (json.has("data")) json.getAsJsonObject("data") else json
                val newAccessToken = data.get("accessToken")?.asString ?: return null
                val newRefreshToken = data.get("refreshToken")?.asString
                preferences.setAccessToken(newAccessToken)
                if (newRefreshToken != null) preferences.setRefreshToken(newRefreshToken)
                newAccessToken
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun responseCount(response: Response): Int {
        var result = 1
        var prior = response.priorResponse
        while (prior != null) {
            result++
            prior = prior.priorResponse
        }
        return result
    }
}

/**
 * Interceptor that unwraps `{ success: true, data: ... }` from successful backend responses
 * so that Retrofit can parse the actual inner data objects directly.
 */
class EnvelopeUnwrappingInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val response = chain.proceed(chain.request())
        if (!response.isSuccessful) return response

        val body = response.body
        if (body != null) {
            val contentType = body.contentType()
            val bodyString = body.string()
            try {
                val jsonElement = JsonParser.parseString(bodyString)
                if (jsonElement.isJsonObject) {
                    val jsonObject = jsonElement.asJsonObject
                    if (jsonObject.has("success")) {
                        val success = jsonObject.get("success").asBoolean
                        if (success && jsonObject.has("data")) {
                            val dataElement = jsonObject.get("data")
                            val newBody = dataElement.toString().toResponseBody(contentType)
                            return response.newBuilder().body(newBody).build()
                        }
                    }
                }
            } catch (e: Exception) {
                // If parsing fails, fall back to original body
            }
            // Recreate response body since body.string() consumed it
            val newBody = bodyString.toResponseBody(contentType)
            return response.newBuilder().body(newBody).build()
        }
        return response
    }
}
