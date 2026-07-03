package com.tionix.rms.feature.merge.data.remote.dto

import com.tionix.rms.feature.merge.domain.model.Merge
import com.tionix.rms.feature.merge.domain.model.MergeStatus
import com.tionix.rms.feature.merge.domain.model.StartMergeRequest

fun StartMergeRequest.toDto(): StartMergeRequestDto {
    return StartMergeRequestDto(
        sourceBoxBarcode = sourceBoxBarcode,
        destinationBoxBarcode = destinationBoxBarcode,
        reason = reason
    )
}

fun MergeDto.toDomain(): Merge {
    return Merge(
        id = id,
        mergeCode = mergeCode,
        sourceBoxBarcode = sourceBoxBarcode,
        sourceBoxName = sourceBoxName,
        destinationBoxBarcode = destinationBoxBarcode,
        destinationBoxName = destinationBoxName,
        status = MergeStatus.valueOf(status),
        reason = reason,
        fileCount = fileCount,
        assignedTo = assignedTo,
        startedAt = startedAt,
        completedAt = completedAt,
        createdAt = createdAt
    )
}
