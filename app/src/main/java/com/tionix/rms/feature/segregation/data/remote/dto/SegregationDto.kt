package com.tionix.rms.feature.segregation.data.remote.dto

data class SegregationDto(
    val id: String,
    val segregationCode: String,
    val boxBarcode: String,
    val boxName: String?,
    val status: String,
    val reasonCode: String?,
    val reason: String?,
    val fileCount: Int,
    val assignedTo: String,
    val startedAt: String?,
    val completedAt: String?,
    val createdAt: String
)
