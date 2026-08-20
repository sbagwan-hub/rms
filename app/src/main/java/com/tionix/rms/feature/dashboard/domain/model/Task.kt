package com.tionix.rms.feature.dashboard.domain.model

data class Task(
    val id: String,
    val taskNumber: String? = null,
    val type: TaskType,
    val title: String,
    val description: String,
    val status: TaskStatus,
    val priority: TaskPriority,
    val assignedTo: String,
    val createdAt: String,
    val dueDate: String?,
    val boxBarcode: String? = null,
    val fileBarcode: String? = null,
    val sourceLocation: String? = null,
    val destinationLocation: String? = null
)

enum class TaskType {
    BOX_SCAN,
    FILE_INSERT,
    FILE_REFILE,
    BOX_TRANSFER,
    SEGREGATION,
    LOCATION_VERIFICATION,
    FILE_VERIFICATION,
    BOX_VERIFICATION,
    CUSTOM,
    FRESH_BOX_MOVE,
    INVENTORY_VERIFICATION,
    REFILE,
    MERGE,
    TRANSFER
}

enum class TaskStatus {
    ASSIGNED,
    ACCEPTED,
    PENDING,
    IN_PROGRESS,
    COMPLETED,
    REJECTED,
    CANCELLED,
    OVERDUE,
    FAILED
}

enum class TaskPriority {
    LOW,
    MEDIUM,
    HIGH,
    URGENT
}
