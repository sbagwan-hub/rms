package com.tionix.rms.feature.dashboard.domain.usecase

import com.tionix.rms.feature.dashboard.domain.model.DashboardStats
import com.tionix.rms.feature.dashboard.domain.repository.DashboardRepository

class GetDashboardStatsUseCase(private val repository: DashboardRepository) {
    suspend operator fun invoke(): Result<DashboardStats> {
        return repository.getDashboardStats()
    }
}
