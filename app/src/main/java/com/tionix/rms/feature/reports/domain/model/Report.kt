package com.tionix.rms.feature.reports.domain.model

data class Report(
    val id: String,
    val type: ReportType,
    val title: String,
    val description: String?,
    val generatedAt: String,
    val generatedBy: String,
    val fileUrl: String?
)

enum class ReportType {
    BOX_MOVEMENT,
    INVENTORY_VERIFICATION,
    REFILE,
    SEGREGATION,
    MERGE,
    TRANSFER,
    AUDIT_LOG
}
