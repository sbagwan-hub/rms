package com.tionix.rms.feature.merge.domain.repository

import com.tionix.rms.feature.merge.domain.model.Merge
import com.tionix.rms.feature.merge.domain.model.StartMergeRequest

interface MergeRepository {
    suspend fun getAssignedMerges(): Result<List<Merge>>
    suspend fun startMerge(request: StartMergeRequest): Result<Merge>
    suspend fun completeMerge(mergeId: String): Result<Unit>
    suspend fun scanBox(barcode: String): Result<Merge?>
}
