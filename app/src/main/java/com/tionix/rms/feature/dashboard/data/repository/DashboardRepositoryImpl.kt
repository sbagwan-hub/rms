package com.tionix.rms.feature.dashboard.data.repository

import com.tionix.rms.core.network.ErrorUtils
import com.tionix.rms.feature.dashboard.data.remote.DashboardApiService
import com.tionix.rms.feature.dashboard.data.remote.DashboardReportsApiService
import com.tionix.rms.feature.dashboard.data.remote.dto.toDomain
import com.tionix.rms.feature.dashboard.domain.model.DashboardStats
import com.tionix.rms.feature.dashboard.domain.model.ReportsSummary
import com.tionix.rms.feature.dashboard.domain.model.Task
import com.tionix.rms.feature.dashboard.domain.model.TaskStatus
import com.tionix.rms.feature.dashboard.domain.repository.DashboardRepository
import javax.inject.Inject

class DashboardRepositoryImpl @Inject constructor(
    private val apiService: DashboardApiService,
    private val reportsApiService: DashboardReportsApiService
) : DashboardRepository {

    override suspend fun getDashboardStats(): Result<DashboardStats> {
        return try {
            val response = apiService.getDashboardStats()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.toDomain())
            } else {
                val errBody = response.errorBody()?.string()
                val serverMsg = try {
                    if (!errBody.isNullOrBlank()) {
                        val json = com.google.gson.JsonParser.parseString(errBody).asJsonObject
                        if (json.has("error")) {
                            val errElem = json.get("error")
                            if (errElem.isJsonObject) errElem.asJsonObject.get("message")?.asString
                            else if (errElem.isJsonPrimitive) errElem.asString
                            else null
                        } else json.get("message")?.asString
                    } else null
                } catch (e: Exception) { null }
                Result.failure(Exception(serverMsg ?: "Failed to fetch dashboard stats (HTTP ${response.code()})"))
            }
        } catch (e: Exception) {
            val msg = e.localizedMessage
            val friendlyMsg = if (!msg.isNullOrBlank() && !msg.contains("Exception") && !msg.contains("java.")) msg else ErrorUtils.getFriendlyErrorMessage(e)
            Result.failure(Exception(friendlyMsg))
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
            Result.failure(Exception(ErrorUtils.getFriendlyErrorMessage(e)))
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
            Result.failure(Exception(ErrorUtils.getFriendlyErrorMessage(e)))
        }
    }

    override suspend fun getReportsSummary(): Result<ReportsSummary> {
        return try {
            val response = reportsApiService.getSummary()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.toDomain())
            } else {
                Result.failure(Exception("Failed to fetch reports summary"))
            }
        } catch (e: Exception) {
            Result.failure(Exception(ErrorUtils.getFriendlyErrorMessage(e)))
        }
    }
}
