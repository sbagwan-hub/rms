package com.tionix.rms.feature.transfer.domain.repository

import com.tionix.rms.feature.transfer.domain.model.Transfer
import com.tionix.rms.feature.transfer.domain.model.StartTransferRequest

interface TransferRepository {
    suspend fun getAssignedTransfers(): Result<List<Transfer>>
    suspend fun startTransfer(request: StartTransferRequest): Result<Transfer>
    suspend fun completeTransfer(transferId: String): Result<Unit>
    suspend fun scanBox(barcode: String): Result<Transfer?>
}
