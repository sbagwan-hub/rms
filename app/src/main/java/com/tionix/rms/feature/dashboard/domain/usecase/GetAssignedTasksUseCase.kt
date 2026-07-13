package com.tionix.rms.feature.dashboard.domain.usecase

import com.tionix.rms.feature.dashboard.domain.model.Task
import com.tionix.rms.feature.dashboard.domain.repository.DashboardRepository
import javax.inject.Inject

class GetAssignedTasksUseCase @Inject constructor(private val repository: DashboardRepository) {
    suspend operator fun invoke(): Result<List<Task>> {
        return repository.getAssignedTasks()
    }
}
