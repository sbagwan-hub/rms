package com.tionix.rms.feature.dashboard.data.remote.dto

import com.tionix.rms.feature.dashboard.domain.model.DashboardStats
import com.tionix.rms.feature.dashboard.domain.model.Task
import com.tionix.rms.feature.dashboard.domain.model.TaskPriority
import com.tionix.rms.feature.dashboard.domain.model.TaskStatus
import com.tionix.rms.feature.dashboard.domain.model.TaskType

fun DashboardStatsDto.toDomain(): DashboardStats {
    return DashboardStats(
        totalTasks = totalTasks,
        pendingTasks = pendingTasks,
        inProgressTasks = inProgressTasks,
        completedTasks = completedTasks,
        urgentTasks = urgentTasks,
        boxesProcessedToday = boxesProcessedToday,
        filesScannedToday = filesScannedToday
    )
}

fun TaskDto.toDomain(): Task {
    return Task(
        id = id,
        type = TaskType.valueOf(type),
        title = title,
        description = description,
        status = TaskStatus.valueOf(status),
        priority = TaskPriority.valueOf(priority),
        assignedTo = assignedTo,
        createdAt = createdAt,
        dueDate = dueDate
    )
}
