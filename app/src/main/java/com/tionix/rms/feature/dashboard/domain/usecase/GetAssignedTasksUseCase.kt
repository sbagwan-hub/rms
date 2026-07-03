package com.tionix.rms.feature.dashboard.domain.usecase

import com.tionix.rms.feature.dashboard.domain.model.Task
import com.tionix.rms.feature.dashboard.domain.repository.DashboardRepository

class GetAssignedTasksUseCase(private val repository: DashboardRepository) {
    suspend operator fun invoke(): Result<List<Task>> {
        return repository.getAssignedTasks()
    }
}
