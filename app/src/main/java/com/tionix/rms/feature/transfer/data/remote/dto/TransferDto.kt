package com.tionix.rms.feature.transfer.data.remote.dto

data class TransferDto(
    val id: String,
    val transferCode: String,
    val boxBarcode: String,
    val boxName: String?,
    val sourceLocation: String,
    val destinationLocation: String,
    val status: String,
    val reason: String?,
    val assignedTo: String,
    val startedAt: String?,
    val completedAt: String?,
    val createdAt: String
)
