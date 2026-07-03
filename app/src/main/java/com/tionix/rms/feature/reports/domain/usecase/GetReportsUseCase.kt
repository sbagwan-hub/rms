package com.tionix.rms.feature.reports.domain.usecase

import com.tionix.rms.feature.reports.domain.model.Report
import com.tionix.rms.feature.reports.domain.model.ReportType
import com.tionix.rms.feature.reports.domain.repository.ReportsRepository

class GetReportsUseCase(private val repository: ReportsRepository) {
    suspend operator fun invoke(type: ReportType? = null): Result<List<Report>> {
        return repository.getReports(type)
    }
}

class GetActivityHistoryUseCase(private val repository: ReportsRepository) {
    suspend operator fun invoke(limit: Int = 50): Result<List<com.tionix.rms.feature.reports.domain.model.ActivityHistory>> {
        return repository.getActivityHistory(limit)
    }
}

class DownloadReportUseCase(private val repository: ReportsRepository) {
    suspend operator fun invoke(reportId: String): Result<String> {
        return repository.downloadReport(reportId)
    }
}
