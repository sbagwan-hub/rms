package com.tionix.rms.feature.freshboxmove.data.remote.dto

import com.tionix.rms.feature.freshboxmove.domain.model.FreshBoxMove
import com.tionix.rms.feature.freshboxmove.domain.model.MoveStatus
import com.tionix.rms.feature.freshboxmove.domain.model.StartMoveRequest

fun StartMoveRequest.toDto(): StartMoveRequestDto {
    return StartMoveRequestDto(
        boxBarcode = boxBarcode,
        destinationLocation = destinationLocation
    )
}

fun FreshBoxMoveDto.toDomain(): FreshBoxMove {
    return FreshBoxMove(
        id = id,
        boxBarcode = boxBarcode,
        boxName = boxName,
        sourceLocation = sourceLocation,
        destinationLocation = destinationLocation,
        status = MoveStatus.valueOf(status),
        assignedTo = assignedTo,
        startedAt = startedAt,
        completedAt = completedAt,
        createdAt = createdAt
    )
}
