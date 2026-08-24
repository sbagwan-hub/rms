package com.tionix.rms.feature.segregation.data.remote.dto

data class SegregationDto(
    val id: String = "",
    val segregationCode: String = "",
    val boxBarcode: String = "",
    val boxName: String? = null,
    val oldBoxId: String? = null,
    val oldBoxBarcode: String? = null,
    val newBoxId: String? = null,
    val newBoxBarcode: String? = null,
    val sourceLocation: String? = null,
    val destinationLocation: String? = null,
    val status: String = "IN_PROGRESS",
    val reasonCode: String? = null,
    val reason: String? = null,
    val fileCount: Int = 0,
    val filesMoved: Int = 0,
    val assignedTo: String? = null,
    val startedAt: String? = null,
    val completedAt: String? = null,
    val createdAt: String? = null
)

data class SegregationDetailResponseDto(
    val success: Boolean = true,
    val data: SegregationDetailDataDto? = null
)

data class SegregationDetailDataDto(
    val id: String = "",
    val segregationCode: String = "",
    val status: String = "IN_PROGRESS",
    val oldBoxId: String = "",
    val oldBoxBarcode: String = "",
    val oldBoxLocation: String = "",
    val newBoxId: String = "",
    val newBoxBarcode: String = "",
    val newBoxLocation: String = "",
    val newBoxCapacity: Int = 25,
    val newBoxCurrentFiles: Int = 0,
    val filesMovedCount: Int = 0,
    val totalFilesCount: Int = 0,
    val startedAt: String? = null,
    val completedAt: String? = null,
    val movedFileBarcodes: List<String> = emptyList()
)

data class ValidateBoxRequestDto(
    val barcode: String,
    val type: String // "OLD_BOX" or "NEW_BOX"
)

data class MoveFileRequestDto(
    val fileBarcode: String,
    val clientEventId: String
)
