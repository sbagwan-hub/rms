package com.tionix.rms.feature.reports.data.remote.dto

import com.tionix.rms.feature.reports.domain.model.ActivityHistory
import com.tionix.rms.feature.reports.domain.model.Report
import com.tionix.rms.feature.reports.domain.model.ReportType

fun ReportDto.toDomain(): Report {
    return Report(
        id = id,
        type = ReportType.valueOf(type),
        title = title,
        description = description,
        generatedAt = generatedAt,
        generatedBy = generatedBy,
        fileUrl = fileUrl
    )
}

fun ActivityHistoryDto.toDomain(): ActivityHistory {
    return ActivityHistory(
        id = id,
        action = action,
        entityType = entityType,
        entityId = entityId,
        description = description,
        performedBy = performedBy,
        performedAt = performedAt,
        metadata = metadata
    )
}
