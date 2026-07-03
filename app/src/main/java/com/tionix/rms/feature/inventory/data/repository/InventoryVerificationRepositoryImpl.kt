package com.tionix.rms.feature.inventory.data.repository

import com.tionix.rms.feature.inventory.data.remote.InventoryApiService
import com.tionix.rms.feature.inventory.domain.model.InventoryVerification
import com.tionix.rms.feature.inventory.domain.model.StartVerificationRequest
import com.tionix.rms.feature.inventory.domain.repository.InventoryVerificationRepository
import javax.inject.Inject

class InventoryVerificationRepositoryImpl @Inject constructor(
    private val apiService: InventoryApiService
) : InventoryVerificationRepository {

    override suspend fun getAssignedVerifications(): Result<List<InventoryVerification>> {
        return try {
            val response = apiService.getAssignedVerifications()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.map { it.toDomain() })
            } else {
                Result.failure(Exception("Failed to fetch verifications"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun startVerification(request: StartVerificationRequest): Result<InventoryVerification> {
        return try {
            val response = apiService.startVerification(request.toDto())
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.toDomain())
            } else {
                Result.failure(Exception("Failed to start verification"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun completeVerification(verificationId: String): Result<Unit> {
        return try {
            val response = apiService.completeVerification(verificationId)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to complete verification"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun scanBox(barcode: String, verificationId: String): Result<Unit> {
        return try {
            val response = apiService.scanBox(verificationId, barcode)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to scan box"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
