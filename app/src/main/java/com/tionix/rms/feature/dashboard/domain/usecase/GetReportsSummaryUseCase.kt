package com.tionix.rms.feature.dashboard.domain.usecase

import com.tionix.rms.feature.dashboard.domain.model.ReportsSummary
import com.tionix.rms.feature.dashboard.domain.repository.DashboardRepository
import javax.inject.Inject

class GetReportsSummaryUseCase @Inject constructor(
    private val repository: DashboardRepository
) {
    suspend operator fun invoke(): Result<ReportsSummary> = repository.getReportsSummary()
}
