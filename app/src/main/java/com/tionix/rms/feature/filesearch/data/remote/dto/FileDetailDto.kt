package com.tionix.rms.feature.filesearch.data.remote.dto

import com.tionix.rms.feature.filesearch.domain.model.FileDetail
import com.tionix.rms.feature.filesearch.domain.model.FileStatus
import com.tionix.rms.feature.filesearch.domain.model.MovementEvent
import com.tionix.rms.feature.filesearch.domain.model.MovementType
import com.tionix.rms.feature.filesearch.domain.model.ParentBox

data class FileDetailResponseEnvelope(
    val success: Boolean = true,
    val message: String? = null,
    val data: FileDetailDto? = null
)

data class FileDetailDto(
    val id: String,
    val barcode: String,
    val title: String,
    val clientName: String? = null,
    val fileType: String? = null,
    val parentBox: ParentBoxDto,
    val locationChain: List<String>,
    val status: String,
    val movementHistory: List<MovementEventDto>,
    val createdAt: String,
    val updatedAt: String?
)

data class ParentBoxDto(
    val id: String,
    val barcode: String,
    val name: String?,
    val location: String,
    val warehouse: String? = null
)

data class MovementEventDto(
    val id: String,
    val eventType: String,
    val fromLocation: String?,
    val toLocation: String?,
    val timestamp: String,
    val performedBy: String,
    val notes: String?
)

fun FileDetailDto.toDomain(): FileDetail = FileDetail(
    id = id,
    barcode = barcode,
    title = title,
    clientName = clientName,
    fileType = fileType,
    parentBox = ParentBox(
        id = parentBox.id,
        barcode = parentBox.barcode,
        name = parentBox.name,
        location = parentBox.location,
        warehouse = parentBox.warehouse
    ),
    locationChain = locationChain,
    status = when (status.uppercase()) {
        "ACTIVE", "IN_BOX" -> FileStatus.ACTIVE
        "CHECKED_OUT" -> FileStatus.CHECKED_OUT
        "ARCHIVED" -> FileStatus.ARCHIVED
        "LOST" -> FileStatus.LOST
        else -> FileStatus.ACTIVE
    },
    movementHistory = movementHistory.map {
        MovementEvent(
            id = it.id,
            eventType = when (it.eventType.uppercase()) {
                "CREATED" -> MovementType.CREATED
                "CHECKED_IN" -> MovementType.CHECKED_IN
                "CHECKED_OUT" -> MovementType.CHECKED_OUT
                "TRANSFERRED" -> MovementType.TRANSFERRED
                "REFILED" -> MovementType.REFILED
                "SEGREGATED" -> MovementType.SEGREGATED
                "MERGED" -> MovementType.MERGED
                else -> MovementType.REFILED
            },
            fromLocation = it.fromLocation,
            toLocation = it.toLocation,
            timestamp = it.timestamp,
            performedBy = it.performedBy,
            notes = it.notes
        )
    },
    createdAt = createdAt,
    updatedAt = updatedAt
)
