package com.tionix.rms.feature.dashboard.data.repository

import com.tionix.rms.feature.dashboard.data.remote.DashboardApiService
import com.tionix.rms.feature.dashboard.domain.model.DashboardStats
import com.tionix.rms.feature.dashboard.domain.model.Task
import com.tionix.rms.feature.dashboard.domain.model.TaskStatus
import com.tionix.rms.feature.dashboard.domain.repository.DashboardRepository
import javax.inject.Inject

class DashboardRepositoryImpl @Inject constructor(
    private val apiService: DashboardApiService
) : DashboardRepository {

    override suspend fun getDashboardStats(): Result<DashboardStats> {
        return try {
            val response = apiService.getDashboardStats()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.toDomain())
            } else {
                Result.failure(Exception("Failed to fetch dashboard stats"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getAssignedTasks(): Result<List<Task>> {
        return try {
            val response = apiService.getAssignedTasks()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.map { it.toDomain() })
            } else {
                Result.failure(Exception("Failed to fetch tasks"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getTasksByStatus(status: TaskStatus): Result<List<Task>> {
        return try {
            val response = apiService.getTasksByStatus(status.name)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.map { it.toDomain() })
            } else {
                Result.failure(Exception("Failed to fetch tasks"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
