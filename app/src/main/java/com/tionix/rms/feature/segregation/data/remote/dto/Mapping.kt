package com.tionix.rms.feature.segregation.data.remote.dto

import com.tionix.rms.feature.segregation.domain.model.Segregation
import com.tionix.rms.feature.segregation.domain.model.SegregationStatus
import com.tionix.rms.feature.segregation.domain.model.StartSegregationRequest

fun StartSegregationRequest.toDto(): StartSegregationRequestDto {
    return StartSegregationRequestDto(
        boxBarcode = boxBarcode,
        reasonCode = reasonCode,
        reason = reason
    )
}

fun SegregationDto.toDomain(): Segregation {
    return Segregation(
        id = id,
        segregationCode = segregationCode,
        boxBarcode = boxBarcode,
        boxName = boxName,
        status = SegregationStatus.valueOf(status),
        reasonCode = reasonCode,
        reason = reason,
        fileCount = fileCount,
        assignedTo = assignedTo,
        startedAt = startedAt,
        completedAt = completedAt,
        createdAt = createdAt
    )
}
