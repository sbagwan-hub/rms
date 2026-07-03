package com.tionix.rms.feature.merge.data.remote.dto

data class MergeDto(
    val id: String,
    val mergeCode: String,
    val sourceBoxBarcode: String,
    val sourceBoxName: String?,
    val destinationBoxBarcode: String,
    val destinationBoxName: String?,
    val status: String,
    val reason: String?,
    val fileCount: Int,
    val assignedTo: String,
    val startedAt: String?,
    val completedAt: String?,
    val createdAt: String
)
