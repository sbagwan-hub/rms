package com.tionix.rms.feature.dashboard.data.remote.dto

import com.tionix.rms.feature.dashboard.domain.model.ReportsSummary

data class ReportsSummaryDto(
    val todayOperationsByType: Map<String, Int> = emptyMap(),
    val missingFilesCount: Int = 0
)

fun ReportsSummaryDto.toDomain(): ReportsSummary =
    ReportsSummary(
        todayOperationsCount = todayOperationsByType.values.sum(),
        missingFilesCount = missingFilesCount
    )
