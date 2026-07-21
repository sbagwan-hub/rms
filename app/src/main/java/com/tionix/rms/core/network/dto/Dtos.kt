package com.tionix.rms.core.network.dto

import com.google.gson.annotations.SerializedName

/**
 * Global response envelope
 */
data class Envelope<T>(
    val success: Boolean,
    val data: T? = null,
    val error: ErrorDetail? = null
)

data class ErrorDetail(
    val code: String,
    val message: String,
    val details: String? = null
)

/**
 * Authentication DTOs
 */
data class DeviceInfo(
    val serialNumber: String,
    val model: String,
    val appVersion: String
)

data class LoginRequest(
    val username: String,
    val password: String,
    val device: DeviceInfo
)

data class UserData(
    val id: String,
    val role: String,
    val permissions: List<String>,
    val warehouses: List<String>
)

data class LoginResponse(
    val accessToken: String,
    val refreshToken: String,
    val user: UserData
)

/**
 * Workflows - Format 1: Fresh Box
 */
data class FreshBoxRequest(
    val clientOpId: String,
    val performedAt: String,
    val locationBarcode: String,
    val boxBarcodes: List<String>
)

data class FreshBoxResponse(
    val operationId: String,
    val warnings: List<String> = emptyList()
)

/**
 * Workflows - Format 2: Inventory Verification
 */
data class InventoryRequest(
    val clientOpId: String,
    val performedAt: String,
    val boxBarcode: String,
    val fileBarcodes: List<String>
)

data class InventorySummary(
    val verified: Int,
    val missing: Int
)

data class InventoryResponse(
    val operationId: String,
    val warnings: List<String> = emptyList(),
    val summary: InventorySummary
)

/**
 * Workflows - Format 3: Refile into Parent Box
 */
data class RefileTargetResponse(
    val expectedLocationBarcode: String,
    val expectedBoxBarcode: String
)

data class RefileRequest(
    val clientOpId: String,
    val performedAt: String,
    val fileBarcode: String,
    val locationBarcode: String,
    val boxBarcode: String
)

/**
 * Workflows - Format 4: Segregation
 */
data class SegregationCheckResponse(
    val fileBarcode: String,
    val action: String // "OUT" | "IN"
)

data class SegregationRequest(
    val clientOpId: String,
    val performedAt: String,
    val oldBoxBarcode: String,
    val newBoxBarcode: String,
    val fileBarcodes: List<String>
)

data class SegregationSummary(
    val out: Int,
    @SerializedName("in")
    val inCount: Int
)

data class SegregationResponse(
    val summary: SegregationSummary,
    val warnings: List<String> = emptyList()
)

/**
 * Standalone Scan event logging
 */
data class ScanRequest(
    val clientOpId: String,
    val barcode: String,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val scannedAt: String
)

/**
 * Site picker (login screen)
 */
data class SiteDto(
    val id: String,
    val name: String,
    val code: String
)
