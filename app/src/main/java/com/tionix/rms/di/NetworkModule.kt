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
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.google.gson.JsonParser
import javax.inject.Singleton

/**
 * Base URL for the mobile-facing backend API surface (`/api/v1/mobile/`).
 * The admin-only surface (`/api/v1/admin/`) is out of scope for this app.
 * 10.0.2.2 is the Android emulator's alias for the host machine's localhost.
 */
private const val BASE_URL = "http://10.0.2.2:4000/api/v1/mobile/"

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
    fun provideOkHttpClient(authInterceptor: Interceptor): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(EnvelopeUnwrappingInterceptor())
            .addInterceptor(logging)
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
