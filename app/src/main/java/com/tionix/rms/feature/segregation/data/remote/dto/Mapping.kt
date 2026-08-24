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
    val segStatus = try {
        SegregationStatus.valueOf(status.uppercase())
    } catch (_: Exception) {
        if (completedAt != null || status.equals("COMPLETED", ignoreCase = true)) {
            SegregationStatus.COMPLETED
        } else {
            SegregationStatus.IN_PROGRESS
        }
    }

    return Segregation(
        id = id,
        segregationCode = segregationCode,
        boxBarcode = boxBarcode,
        boxName = boxName,
        oldBoxBarcode = oldBoxBarcode ?: boxBarcode,
        newBoxBarcode = newBoxBarcode,
        sourceLocation = sourceLocation,
        destinationLocation = destinationLocation,
        status = segStatus,
        reasonCode = reasonCode,
        reason = reason,
        fileCount = fileCount,
        filesMoved = filesMoved,
        assignedTo = assignedTo,
        startedAt = startedAt,
        completedAt = completedAt,
        createdAt = createdAt
    )
}
