package com.tionix.rms.feature.dashboard.data.remote.dto

data class TaskDto(
    val id: String,
    val type: String,
    val title: String,
    val description: String,
    val status: String,
    val priority: String,
    val assignedTo: String,
    val createdAt: String,
    val dueDate: String?
)
