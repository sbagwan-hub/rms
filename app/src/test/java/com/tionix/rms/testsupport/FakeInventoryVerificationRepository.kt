package com.tionix.rms.testsupport

import com.tionix.rms.feature.inventory.domain.model.Box
import com.tionix.rms.feature.inventory.domain.model.InventoryVerification
import com.tionix.rms.feature.inventory.domain.model.ScanStatus
import com.tionix.rms.feature.inventory.domain.model.ScannedBox
import com.tionix.rms.feature.inventory.domain.model.StartVerificationRequest
import com.tionix.rms.feature.inventory.domain.model.VerificationStatus
import com.tionix.rms.feature.inventory.domain.repository.InventoryVerificationRepository

class FakeInventoryVerificationRepository : InventoryVerificationRepository {
    private val verifiedBarcodes = mutableSetOf<String>()
    var serverWarnings: List<String> = emptyList()

    override suspend fun getAssignedVerifications(): Result<List<InventoryVerification>> =
        Result.success(emptyList())

    override suspend fun startVerification(request: StartVerificationRequest): Result<InventoryVerification> {
        return Result.success(
            InventoryVerification(
                id = "ver-1",
                verificationCode = "IV-1",
                locationId = request.locationId,
                locationName = "Location A",
                status = VerificationStatus.IN_PROGRESS,
                totalBoxes = 1,
                verifiedBoxes = 0,
                discrepancyBoxes = 0,
                assignedTo = "operator",
                startedAt = null,
                completedAt = null,
                createdAt = "2026-01-01"
            )
        )
    }

    override suspend fun getExpectedBoxes(locationId: String): Result<List<Box>> =
        Result.success(
            listOf(
                Box(
                    id = "box-1",
                    barcode = "BOX-001",
                    description = "Box",
                    currentLocation = locationId,
                    expectedLocation = locationId
                )
            )
        )

    override suspend fun verifyBox(barcode: String, verificationId: String): Result<ScannedBox> {
        if (verifiedBarcodes.contains(barcode)) {
            return Result.success(
                ScannedBox(
                    barcode = barcode,
                    scanStatus = ScanStatus.DUPLICATE,
                    timestamp = "2026-01-01T00:00:00Z"
                )
            )
        }
        verifiedBarcodes.add(barcode)
        return Result.success(
            ScannedBox(
                barcode = barcode,
                scanStatus = ScanStatus.VERIFIED,
                timestamp = "2026-01-01T00:00:00Z"
            )
        )
    }

    override suspend fun completeVerification(verificationId: String): Result<Unit> =
        Result.success(Unit)

    override suspend fun syncVerificationToQueue(verificationId: String): Result<Unit> =
        Result.success(Unit)
}
