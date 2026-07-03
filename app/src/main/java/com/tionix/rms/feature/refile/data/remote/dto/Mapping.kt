package com.tionix.rms.feature.refile.data.remote.dto

import com.tionix.rms.feature.refile.domain.model.Refile
import com.tionix.rms.feature.refile.domain.model.RefileStatus
import com.tionix.rms.feature.refile.domain.model.StartRefileRequest

fun StartRefileRequest.toDto(): StartRefileRequestDto {
    return StartRefileRequestDto(
        fileBarcode = fileBarcode,
        newLocation = newLocation,
        reason = reason
    )
}

fun RefileDto.toDomain(): Refile {
    return Refile(
        id = id,
        refileCode = refileCode,
        fileBarcode = fileBarcode,
        fileName = fileName,
        currentLocation = currentLocation,
        newLocation = newLocation,
        status = RefileStatus.valueOf(status),
        reason = reason,
        assignedTo = assignedTo,
        startedAt = startedAt,
        completedAt = completedAt,
        createdAt = createdAt
    )
}
