package com.tionix.rms.feature.merge.domain.repository

import com.tionix.rms.feature.merge.domain.model.Box
import com.tionix.rms.feature.merge.domain.model.Merge
import com.tionix.rms.feature.merge.domain.model.MergeSession
import com.tionix.rms.feature.merge.domain.model.StartMergeRequest

interface MergeRepository {
    suspend fun getAssignedMerges(): Result<List<Merge>>
    suspend fun startMerge(request: StartMergeRequest): Result<Merge>
    suspend fun completeMerge(mergeId: String): Result<Unit>
    suspend fun scanBox(barcode: String): Result<Merge?>
    suspend fun startMergeSession(): Result<MergeSession>
    suspend fun scanDestinationBox(barcode: String): Result<Box>
    suspend fun scanSourceBox(sessionId: String, barcode: String): Result<Box>
    suspend fun removeSourceBox(sessionId: String, boxBarcode: String): Result<Unit>
    suspend fun submitMerge(sessionId: String): Result<Unit>
    suspend fun syncMergeToQueue(sessionId: String): Result<Unit>
}
