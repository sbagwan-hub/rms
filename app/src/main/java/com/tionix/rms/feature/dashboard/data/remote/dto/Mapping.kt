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
    val safeType = try {
        TaskType.valueOf(type.uppercase())
    } catch (e: Exception) {
        TaskType.CUSTOM
    }

    val safeStatus = try {
        TaskStatus.valueOf(status.uppercase())
    } catch (e: Exception) {
        TaskStatus.ASSIGNED
    }

    val safePriority = try {
        TaskPriority.valueOf(priority.uppercase())
    } catch (e: Exception) {
        TaskPriority.MEDIUM
    }

    return Task(
        id = id,
        taskNumber = taskNumber,
        type = safeType,
        title = title,
        description = description ?: "",
        status = safeStatus,
        priority = safePriority,
        assignedTo = assignedTo ?: "Unassigned",
        createdAt = createdAt,
        dueDate = dueDate,
        boxBarcode = boxBarcode,
        fileBarcode = fileBarcode,
        sourceLocation = sourceLocation,
        destinationLocation = destinationLocation
    )
}
