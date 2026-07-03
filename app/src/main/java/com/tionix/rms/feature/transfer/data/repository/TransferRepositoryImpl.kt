package com.tionix.rms.feature.transfer.data.repository

import com.tionix.rms.feature.transfer.data.remote.TransferApiService
import com.tionix.rms.feature.transfer.domain.model.StartTransferRequest
import com.tionix.rms.feature.transfer.domain.model.Transfer
import com.tionix.rms.feature.transfer.domain.repository.TransferRepository
import javax.inject.Inject

class TransferRepositoryImpl @Inject constructor(
    private val apiService: TransferApiService
) : TransferRepository {

    override suspend fun getAssignedTransfers(): Result<List<Transfer>> {
        return try {
            val response = apiService.getAssignedTransfers()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.map { it.toDomain() })
            } else {
                Result.failure(Exception("Failed to fetch transfers"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun startTransfer(request: StartTransferRequest): Result<Transfer> {
        return try {
            val response = apiService.startTransfer(request.toDto())
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.toDomain())
            } else {
                Result.failure(Exception("Failed to start transfer"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun completeTransfer(transferId: String): Result<Unit> {
        return try {
            val response = apiService.completeTransfer(transferId)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to complete transfer"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun scanBox(barcode: String): Result<Transfer?> {
        return try {
            val response = apiService.scanBox(barcode)
            if (response.isSuccessful) {
                Result.success(response.body()?.toDomain())
            } else {
                Result.failure(Exception("Failed to scan box"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
