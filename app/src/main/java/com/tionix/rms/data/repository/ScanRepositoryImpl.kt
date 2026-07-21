package com.tionix.rms.data.repository

import com.tionix.rms.core.network.ApiService
import com.tionix.rms.core.network.dto.LookupData
import com.tionix.rms.core.network.dto.ScanRequest
import com.google.gson.JsonParser
import java.time.Instant
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ScanRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
) : ScanRepository {

    override suspend fun lookup(barcode: String): Result<LookupData> {
        val code = barcode.trim()
        return try {
            val response = apiService.lookup(code)
            val body = response.body()
            if (response.isSuccessful && body != null) {
                Result.success(body)
            } else {
                Result.failure(Exception(parseErrorMessage(response.errorBody()?.string(), code)))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun record(barcode: String, lat: Double?, lng: Double?): Result<Unit> {
        return try {
            val response = apiService.recordScan(
                ScanRequest(
                    clientOpId = UUID.randomUUID().toString(),
                    barcode = barcode.trim(),
                    latitude = lat,
                    longitude = lng,
                    scannedAt = Instant.now().toString(),
                )
            )
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(parseErrorMessage(response.errorBody()?.string(), barcode)))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** Backend error envelope: { success: false, error: { code, message } }. */
    private fun parseErrorMessage(errorBody: String?, barcode: String): String {
        if (errorBody.isNullOrBlank()) return "Lookup failed for '$barcode'"
        return try {
            val json = JsonParser.parseString(errorBody).asJsonObject
            val error = json.getAsJsonObject("error")
            val code = error?.get("code")?.asString
            val message = error?.get("message")?.asString ?: "Lookup failed for '$barcode'"
            if (code != null) "$code: $message" else message
        } catch (e: Exception) {
            "Lookup failed for '$barcode'"
        }
    }
}
