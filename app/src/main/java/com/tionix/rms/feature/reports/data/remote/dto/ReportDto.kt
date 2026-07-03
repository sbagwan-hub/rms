package com.tionix.rms.feature.reports.data.remote.dto

data class ReportDto(
    val id: String,
    val type: String,
    val title: String,
    val description: String?,
    val generatedAt: String,
    val generatedBy: String,
    val fileUrl: String?
)
