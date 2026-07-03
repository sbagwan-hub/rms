package com.tionix.rms.feature.transfer.data.remote.dto

import com.tionix.rms.feature.transfer.domain.model.StartTransferRequest
import com.tionix.rms.feature.transfer.domain.model.Transfer
import com.tionix.rms.feature.transfer.domain.model.TransferStatus

fun StartTransferRequest.toDto(): StartTransferRequestDto {
    return StartTransferRequestDto(
        boxBarcode = boxBarcode,
        destinationLocation = destinationLocation,
        reason = reason
    )
}

fun TransferDto.toDomain(): Transfer {
    return Transfer(
        id = id,
        transferCode = transferCode,
        boxBarcode = boxBarcode,
        boxName = boxName,
        sourceLocation = sourceLocation,
        destinationLocation = destinationLocation,
        status = TransferStatus.valueOf(status),
        reason = reason,
        assignedTo = assignedTo,
        startedAt = startedAt,
        completedAt = completedAt,
        createdAt = createdAt
    )
}
