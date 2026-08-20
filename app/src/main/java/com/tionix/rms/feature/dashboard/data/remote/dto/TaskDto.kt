package com.tionix.rms.feature.dashboard.data.remote.dto

data class TaskDto(
    val id: String,
    val taskNumber: String? = null,
    val type: String,
    val title: String,
    val description: String? = null,
    val status: String,
    val priority: String,
    val assignedTo: String? = null,
    val createdAt: String,
    val dueDate: String? = null,
    val boxBarcode: String? = null,
    val fileBarcode: String? = null,
    val sourceLocation: String? = null,
    val destinationLocation: String? = null
)
