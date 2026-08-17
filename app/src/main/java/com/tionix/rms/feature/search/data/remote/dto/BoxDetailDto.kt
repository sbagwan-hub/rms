package com.tionix.rms.feature.search.data.remote.dto

import com.tionix.rms.feature.search.domain.model.BoxDetail
import com.tionix.rms.feature.search.domain.model.BoxStatus
import com.tionix.rms.feature.search.domain.model.FileRecord

data class BoxDetailDto(
    val id: String,
    val barcode: String,
    val name: String?,
    val location: String,
    val status: String,
    val fileCount: Int,
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
    location = location,
    status = when (status.uppercase()) {
        "ACTIVE" -> BoxStatus.ACTIVE
        "ARCHIVED" -> BoxStatus.ARCHIVED
        "DESTROYED" -> BoxStatus.DESTROYED
        "IN_TRANSIT" -> BoxStatus.IN_TRANSIT
        "MERGED" -> BoxStatus.MERGED
        else -> BoxStatus.ACTIVE
    },
    fileCount = fileCount,
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
