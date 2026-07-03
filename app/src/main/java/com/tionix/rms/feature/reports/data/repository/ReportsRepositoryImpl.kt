package com.tionix.rms.feature.reports.data.repository

import com.tionix.rms.feature.reports.data.remote.ReportsApiService
import com.tionix.rms.feature.reports.domain.model.ActivityHistory
import com.tionix.rms.feature.reports.domain.model.Report
import com.tionix.rms.feature.reports.domain.model.ReportType
import com.tionix.rms.feature.reports.domain.repository.ReportsRepository
import javax.inject.Inject

class ReportsRepositoryImpl @Inject constructor(
    private val apiService: ReportsApiService
) : ReportsRepository {

    override suspend fun getReports(type: ReportType?): Result<List<Report>> {
        return try {
            val response = apiService.getReports(type?.name)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.map { it.toDomain() })
            } else {
                Result.failure(Exception("Failed to fetch reports"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getActivityHistory(limit: Int): Result<List<ActivityHistory>> {
        return try {
            val response = apiService.getActivityHistory(limit)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.map { it.toDomain() })
            } else {
                Result.failure(Exception("Failed to fetch activity history"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun downloadReport(reportId: String): Result<String> {
        return try {
            val response = apiService.downloadReport(reportId)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to download report"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
