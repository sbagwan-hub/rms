package com.tionix.rms.feature.freshboxmove.data.repository

import com.tionix.rms.feature.freshboxmove.data.remote.FreshBoxMoveApiService
import com.tionix.rms.feature.freshboxmove.domain.model.FreshBoxMove
import com.tionix.rms.feature.freshboxmove.domain.model.MoveStatus
import com.tionix.rms.feature.freshboxmove.domain.model.StartMoveRequest
import com.tionix.rms.feature.freshboxmove.domain.repository.FreshBoxMoveRepository
import javax.inject.Inject

class FreshBoxMoveRepositoryImpl @Inject constructor(
    private val apiService: FreshBoxMoveApiService
) : FreshBoxMoveRepository {

    override suspend fun getAssignedMoves(): Result<List<FreshBoxMove>> {
        return try {
            val response = apiService.getAssignedMoves()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.map { it.toDomain() })
            } else {
                Result.failure(Exception("Failed to fetch moves"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun startMove(request: StartMoveRequest): Result<FreshBoxMove> {
        return try {
            val response = apiService.startMove(request.toDto())
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.toDomain())
            } else {
                Result.failure(Exception("Failed to start move"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun completeMove(moveId: String): Result<Unit> {
        return try {
            val response = apiService.completeMove(moveId)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to complete move"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun scanBox(barcode: String): Result<FreshBoxMove?> {
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
