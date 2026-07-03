package com.tionix.rms.core.scanner.domain.model

data class ScanResult(
    val barcode: String,
    val scanType: ScanType,
    val timestamp: Long,
    val rawData: String?
)

enum class ScanType {
    BARCODE,
    QR_CODE
}
