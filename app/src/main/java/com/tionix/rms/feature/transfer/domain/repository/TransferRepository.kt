package com.tionix.rms.feature.transfer.domain.repository

import com.tionix.rms.feature.transfer.domain.model.Transfer
import com.tionix.rms.feature.transfer.domain.model.TransferItem
import com.tionix.rms.feature.transfer.domain.model.TransferSession
import com.tionix.rms.feature.transfer.domain.model.StartTransferRequest

interface TransferRepository {
    suspend fun getAssignedTransfers(): Result<List<Transfer>>
    suspend fun startTransfer(request: StartTransferRequest): Result<Transfer>
    suspend fun completeTransfer(transferId: String): Result<Unit>
    suspend fun scanBox(barcode: String): Result<Transfer?>
    suspend fun startTransferSession(transferType: com.tionix.rms.feature.transfer.domain.model.TransferType): Result<TransferSession>
    suspend fun addTransferItem(sessionId: String, barcode: String): Result<TransferItem>
    suspend fun removeTransferItem(sessionId: String, itemId: String): Result<Unit>
    suspend fun setDestination(sessionId: String, destination: String): Result<Unit>
    suspend fun submitTransfer(sessionId: String): Result<Unit>
    suspend fun syncTransferToQueue(transferId: String): Result<Unit>
}
