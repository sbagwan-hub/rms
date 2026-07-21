package com.tionix.rms.data.repository

import com.tionix.rms.core.network.dto.ContentItem
import com.tionix.rms.core.network.dto.EntityData
import com.tionix.rms.core.network.dto.LookupData
import kotlinx.coroutines.delay
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ScanRepositoryImpl @Inject constructor() : ScanRepository {
    override suspend fun lookup(barcode: String): Result<LookupData> {
        delay(800) // Simulating network delay
        val code = barcode.trim().uppercase()
        
        // Handle invalid barcode simulations
        if (code == "ERROR") {
            return Result.failure(Exception("Barcode is registered to a different company"))
        }
        if (code == "UNKNOWN") {
            return Result.failure(Exception("BARCODE_UNKNOWN: Barcode not found in the database"))
        }

        val lookupData = when {
            code.startsWith("LOC") -> {
                LookupData(
                    entityType = "LOCATION",
                    entity = EntityData(
                        barcode = code,
                        status = "ACTIVE",
                        capacity = 10,
                        occupied = 3
                    ),
                    path = listOf("Main Warehouse", "Zone A", "Room 3", "Rack 1", "Shelf 2", code),
                    contents = listOf(
                        ContentItem(id = "1", barcode = "BOX-001", label = "Finance Q3 Docs", status = "SEALED", fileCount = 24),
                        ContentItem(id = "2", barcode = "BOX-002", label = "HR Employee Files", status = "OPEN", fileCount = 15),
                        ContentItem(id = "3", barcode = "BOX-003", label = "Legal Contracts 2025", status = "SEALED", fileCount = 8)
                    )
                )
            }
            code.startsWith("BOX") -> {
                LookupData(
                    entityType = "BOX",
                    entity = EntityData(
                        barcode = code,
                        status = "IN_TRANSIT"
                    ),
                    path = listOf("Main Warehouse", "Zone A", "Room 3", "Rack 1", "Shelf 2", "LOC-042", code),
                    contents = listOf(
                        ContentItem(id = "f1", barcode = "FILE-001", label = "Invoice #28471", status = "VERIFIED"),
                        ContentItem(id = "f2", barcode = "FILE-002", label = "Tax Return 2024", status = "PENDING"),
                        ContentItem(id = "f3", barcode = "FILE-003", label = "NDA - Acme Corp", status = "VERIFIED")
                    )
                )
            }
            code.startsWith("FILE") -> {
                LookupData(
                    entityType = "FILE",
                    entity = EntityData(
                        barcode = code,
                        status = "ALLOCATED"
                    ),
                    path = listOf("Main Warehouse", "Zone A", "Room 3", "Rack 1", "Shelf 2", "LOC-042", "BOX-001", code),
                    contents = emptyList()
                )
            }
            else -> {
                return Result.failure(Exception("BARCODE_UNKNOWN: Barcode '$barcode' is not registered in this warehouse database."))
            }
        }
        return Result.success(lookupData)
    }

    override suspend fun record(barcode: String, lat: Double?, lng: Double?): Result<Unit> {
        delay(400) // Simulating network write
        return Result.success(Unit)
    }
}
