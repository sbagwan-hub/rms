package com.tionix.rms.feature.history.data.remote.dto

data class OperationsListResponseDto(
    val data: List<OperationSummaryDto> = emptyList(),
    val meta: OperationsMetaDto? = null
)

data class OperationsMetaDto(
    val page: Int? = null,
    val limit: Int? = null,
    val total: Int? = null,
    val totalPages: Int? = null
)

data class OperationSummaryDto(
    val id: String,
    val type: String,
    val status: String,
    val performedAt: String,
    val summary: String,
    val reasonCode: String? = null,
    val boxBarcode: String? = null,
    val fileBarcode: String? = null,
    val warehouseName: String? = null,
    val verifiedCount: Int? = null,
    val missingCount: Int? = null,
    val warningsCount: Int? = null
)
