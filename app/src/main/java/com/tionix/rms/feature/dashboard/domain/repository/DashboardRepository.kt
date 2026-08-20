package com.tionix.rms.feature.dashboard.domain.repository

import com.tionix.rms.feature.dashboard.domain.model.ReportsSummary
import com.tionix.rms.feature.dashboard.domain.model.DashboardStats
import com.tionix.rms.feature.dashboard.domain.model.Task

interface DashboardRepository {
    suspend fun getDashboardStats(): Result<DashboardStats>
    suspend fun getAssignedTasks(): Result<List<Task>>
    suspend fun getTasksByStatus(status: com.tionix.rms.feature.dashboard.domain.model.TaskStatus): Result<List<Task>>
    suspend fun getReportsSummary(): Result<ReportsSummary>
    suspend fun acceptTask(taskId: String): Result<Boolean>
    suspend fun startTask(taskId: String): Result<Boolean>
    suspend fun completeTask(taskId: String, payload: Map<String, String>): Result<Boolean>
    suspend fun rejectTask(taskId: String, reason: String): Result<Boolean>
}
