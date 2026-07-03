package com.tionix.rms.feature.refile.domain.repository

import com.tionix.rms.feature.refile.domain.model.Refile
import com.tionix.rms.feature.refile.domain.model.StartRefileRequest

interface RefileRepository {
    suspend fun getAssignedRefiles(): Result<List<Refile>>
    suspend fun startRefile(request: StartRefileRequest): Result<Refile>
    suspend fun completeRefile(refileId: String): Result<Unit>
    suspend fun scanFile(barcode: String): Result<Refile?>
}
