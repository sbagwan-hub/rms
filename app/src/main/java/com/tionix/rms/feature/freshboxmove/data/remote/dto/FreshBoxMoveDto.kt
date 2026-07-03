package com.tionix.rms.feature.freshboxmove.data.remote.dto

data class FreshBoxMoveDto(
    val id: String,
    val boxBarcode: String,
    val boxName: String?,
    val sourceLocation: String,
    val destinationLocation: String,
    val status: String,
    val assignedTo: String,
    val startedAt: String?,
    val completedAt: String?,
    val createdAt: String
)
