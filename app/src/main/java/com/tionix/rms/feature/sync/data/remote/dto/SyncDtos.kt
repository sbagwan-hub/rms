package com.tionix.rms.feature.sync.data.remote.dto

data class SyncOperationsRequestDto(
    val operations: List<SyncOperationDto>
)

data class SyncOperationDto(
    val type: String,
    val payload: Map<String, @JvmSuppressWildcards Any?>
)

data class SyncOperationResultDto(
    val clientOpId: String,
    val status: String,
    val operationId: String? = null,
    val error: String? = null
)
