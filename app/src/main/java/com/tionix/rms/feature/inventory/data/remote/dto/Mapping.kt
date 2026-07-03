package com.tionix.rms.feature.inventory.data.remote.dto

import com.tionix.rms.feature.inventory.domain.model.InventoryVerification
import com.tionix.rms.feature.inventory.domain.model.StartVerificationRequest
import com.tionix.rms.feature.inventory.domain.model.VerificationStatus

fun StartVerificationRequest.toDto(): StartVerificationRequestDto {
    return StartVerificationRequestDto(locationId = locationId)
}

fun InventoryVerificationDto.toDomain(): InventoryVerification {
    return InventoryVerification(
        id = id,
        verificationCode = verificationCode,
        locationId = locationId,
        locationName = locationName,
        status = VerificationStatus.valueOf(status),
        totalBoxes = totalBoxes,
        verifiedBoxes = verifiedBoxes,
        discrepancyBoxes = discrepancyBoxes,
        assignedTo = assignedTo,
        startedAt = startedAt,
        completedAt = completedAt,
        createdAt = createdAt
    )
}
