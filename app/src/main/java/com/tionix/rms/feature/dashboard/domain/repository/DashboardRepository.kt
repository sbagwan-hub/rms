package com.tionix.rms.feature.dashboard.domain.repository

import com.tionix.rms.feature.dashboard.domain.model.DashboardStats
import com.tionix.rms.feature.dashboard.domain.model.Task

interface DashboardRepository {
    suspend fun getDashboardStats(): Result<DashboardStats>
    suspend fun getAssignedTasks(): Result<List<Task>>
    suspend fun getTasksByStatus(status: com.tionix.rms.feature.dashboard.domain.model.TaskStatus): Result<List<Task>>
}
