package com.tionix.rms.core.network.dto

data class LookupData(
    val entityType: String, // "LOCATION", "BOX", "FILE"
    val entity: EntityData,
    val path: List<String>,
    val contents: List<ContentItem>
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
