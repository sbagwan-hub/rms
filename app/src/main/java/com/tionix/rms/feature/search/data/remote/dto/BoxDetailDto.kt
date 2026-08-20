package com.tionix.rms.feature.search.data.remote.dto

import com.tionix.rms.feature.search.domain.model.BoxDetail
import com.tionix.rms.feature.search.domain.model.BoxStatus
import com.tionix.rms.feature.search.domain.model.FileRecord

data class BoxDetailDto(
    val id: String,
    val barcode: String,
    val name: String?,
    val boxType: String? = null,
    val warehouse: String? = null,
    val site: String? = null,
    val location: String,
    val status: String,
    val fileCount: Int,
    val capacity: Int? = 50,
    val availableSlots: Int? = 50,
    val lastActivity: String?,
    val contents: List<FileRecordDto>,
    val clientId: String,
    val clientName: String?
)

data class FileRecordDto(
    val id: String,
    val barcode: String,
    val title: String,
    val boxBarcode: String
)

fun BoxDetailDto.toDomain(): BoxDetail = BoxDetail(
    id = id,
    barcode = barcode,
    name = name,
    boxType = boxType ?: "STANDARD",
    warehouse = warehouse ?: "Unassigned",
    site = site ?: "Unassigned",
    location = location,
    status = when (status.uppercase()) {
        "ACTIVE" -> BoxStatus.ACTIVE
        "ARCHIVED" -> BoxStatus.ARCHIVED
        "LOCKED" -> BoxStatus.LOCKED
        "IN_TRANSIT" -> BoxStatus.IN_TRANSIT
        else -> BoxStatus.ACTIVE
    },
    fileCount = fileCount,
    capacity = capacity ?: 50,
    availableSlots = availableSlots ?: Math.max(0, (capacity ?: 50) - fileCount),
    lastActivity = lastActivity,
    contents = contents.map {
        FileRecord(
            id = it.id,
            barcode = it.barcode,
            title = it.title,
            boxBarcode = it.boxBarcode
        )
    },
    clientId = clientId,
    clientName = clientName
)
