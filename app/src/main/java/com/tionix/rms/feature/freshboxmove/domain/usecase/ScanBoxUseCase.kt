package com.tionix.rms.feature.freshboxmove.domain.usecase

import com.tionix.rms.feature.freshboxmove.domain.model.FreshBoxMove
import com.tionix.rms.feature.freshboxmove.domain.repository.FreshBoxMoveRepository

class ScanBoxUseCase(private val repository: FreshBoxMoveRepository) {
    suspend operator fun invoke(barcode: String): Result<FreshBoxMove?> {
        return repository.scanBox(barcode)
    }
}
