package com.tionix.rms.feature.reports.data.remote.dto

data class ActivityHistoryDto(
    val id: String,
    val action: String,
    val entityType: String,
    val entityId: String,
    val description: String,
    val performedBy: String,
    val performedAt: String,
    val metadata: Map<String, String>?
)
