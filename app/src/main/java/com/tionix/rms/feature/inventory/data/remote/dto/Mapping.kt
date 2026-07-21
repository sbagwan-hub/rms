package com.tionix.rms.feature.inventory.data.remote.dto

import com.tionix.rms.feature.inventory.domain.model.InventoryVerification
import com.tionix.rms.feature.inventory.domain.model.StartVerificationRequest
import com.tionix.rms.feature.inventory.domain.model.VerificationStatus

fun StartVerificationRequest.toDto(boxId: String): StartVerificationRequestDto {
    return StartVerificationRequestDto(boxId = boxId)
}

fun InventoryVerificationDto.toDomain(): InventoryVerification {
    return InventoryVerification(
        id = id,
        verificationCode = "INV-${id.take(8).uppercase()}",
        locationId = boxId,
        locationName = box.barcode,
        status = if (endedAt != null) VerificationStatus.COMPLETED else VerificationStatus.IN_PROGRESS,
        totalBoxes = scans?.size ?: 0,
        verifiedBoxes = scans?.count { it.isExpected } ?: 0,
        discrepancyBoxes = unexpectedFileCount,
        assignedTo = operator.fullName,
        startedAt = startedAt,
        completedAt = endedAt,
        createdAt = startedAt
    )
}
