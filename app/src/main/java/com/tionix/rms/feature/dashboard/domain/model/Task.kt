package com.tionix.rms.feature.dashboard.domain.model

data class Task(
    val id: String,
    val type: TaskType,
    val title: String,
    val description: String,
    val status: TaskStatus,
    val priority: TaskPriority,
    val assignedTo: String,
    val createdAt: String,
    val dueDate: String?
)

enum class TaskType {
    FRESH_BOX_MOVE,
    INVENTORY_VERIFICATION,
    REFILE,
    SEGREGATION,
    MERGE,
    TRANSFER
}

enum class TaskStatus {
    PENDING,
    IN_PROGRESS,
    COMPLETED,
    FAILED
}

enum class TaskPriority {
    LOW,
    MEDIUM,
    HIGH,
    URGENT
}
