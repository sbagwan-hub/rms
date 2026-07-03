package com.tionix.rms.feature.refile.data.remote.dto

data class RefileDto(
    val id: String,
    val refileCode: String,
    val fileBarcode: String,
    val fileName: String?,
    val currentLocation: String,
    val newLocation: String,
    val status: String,
    val reason: String?,
    val assignedTo: String,
    val startedAt: String?,
    val completedAt: String?,
    val createdAt: String
)
