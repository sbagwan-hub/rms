package com.tionix.rms.core.network

import android.os.Build
import com.tionix.rms.core.settings.AppSettingsStore
import kotlinx.coroutines.runBlocking
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Dynamically rewrites HTTP request host, port, and scheme to match the
 * user-configured server URL in AppSettingsStore (e.g. physical device LAN IP or USB reverse).
 * Also automatically converts emulator-only 10.0.2.2 addresses to 127.0.0.1 on physical devices.
 */
@Singleton
class DynamicHostInterceptor @Inject constructor(
    private val appSettingsStore: AppSettingsStore
) : Interceptor {

    private fun isEmulator(): Boolean {
        return (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic"))
                || Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.startsWith("unknown")
                || Build.HARDWARE.contains("goldfish")
                || Build.HARDWARE.contains("ranchu")
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for x86")
                || Build.MANUFACTURER.contains("Genymotion")
                || Build.PRODUCT.contains("sdk_google")
                || Build.PRODUCT.contains("google_sdk")
                || Build.PRODUCT.contains("sdk")
                || Build.PRODUCT.contains("sdk_x86")
                || Build.PRODUCT.contains("vbox86p")
                || Build.PRODUCT.contains("emulator")
                || Build.PRODUCT.contains("simulator")
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()
        val configuredUrl = runBlocking { appSettingsStore.getServerUrl() }

        var targetUrl = configuredUrl
        if (!isEmulator()) {
            if (targetUrl.isNullOrBlank()) {
                targetUrl = com.tionix.rms.BuildConfig.API_BASE_URL
            } else if (targetUrl.contains("10.0.2.2") || request.url.host == "10.0.2.2") {
                targetUrl = targetUrl.replace("10.0.2.2", "192.168.1.7")
            }
        }

        if (!targetUrl.isNullOrBlank()) {
            val parsedUrl = targetUrl.toHttpUrlOrNull()
            if (parsedUrl != null) {
                val originalUrl = request.url
                val newUrl = originalUrl.newBuilder()
                    .scheme(parsedUrl.scheme)
                    .host(parsedUrl.host)
                    .port(parsedUrl.port)
                    .build()
                request = request.newBuilder().url(newUrl).build()
            }
        }
        return chain.proceed(request)
    }
}
