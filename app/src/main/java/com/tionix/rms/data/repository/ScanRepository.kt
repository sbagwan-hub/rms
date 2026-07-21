package com.tionix.rms.data.repository

import com.tionix.rms.core.network.dto.LookupData

interface ScanRepository {
    suspend fun lookup(barcode: String): Result<LookupData>
    suspend fun record(barcode: String, lat: Double?, lng: Double?): Result<Unit>
}
