package com.tionix.rms.core.network.dto

data class LookupData(
    val entityType: String, // "LOCATION", "BOX", "FILE"
    val entity: EntityData,
    val path: List<PathSegment>,
    val contents: List<ContentItem>
)

/** Matches the backend's `{ type, name }` breadcrumb segment shape. */
data class PathSegment(
    val type: String,
    val name: String
)

data class EntityData(
    val barcode: String,
    val status: String? = null,
    val capacity: Int? = null,
    val occupied: Int? = null
)

data class ContentItem(
    val id: String,
    val barcode: String,
    val label: String? = null,
    val status: String? = null,
    val fileCount: Int? = null
)
