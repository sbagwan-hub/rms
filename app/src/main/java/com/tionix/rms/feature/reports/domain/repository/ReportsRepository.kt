package com.tionix.rms.feature.reports.domain.repository

import com.tionix.rms.feature.reports.domain.model.ActivityHistory
import com.tionix.rms.feature.reports.domain.model.Report
import com.tionix.rms.feature.reports.domain.model.ReportType

interface ReportsRepository {
    suspend fun getReports(type: ReportType?): Result<List<Report>>
    suspend fun getActivityHistory(limit: Int): Result<List<ActivityHistory>>
    suspend fun downloadReport(reportId: String): Result<String>
}
