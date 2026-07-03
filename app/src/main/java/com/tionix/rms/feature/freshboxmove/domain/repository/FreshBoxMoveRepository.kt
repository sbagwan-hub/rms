package com.tionix.rms.feature.freshboxmove.domain.repository

import com.tionix.rms.feature.freshboxmove.domain.model.FreshBoxMove
import com.tionix.rms.feature.freshboxmove.domain.model.MoveStatus
import com.tionix.rms.feature.freshboxmove.domain.model.StartMoveRequest

interface FreshBoxMoveRepository {
    suspend fun getAssignedMoves(): Result<List<FreshBoxMove>>
    suspend fun startMove(request: StartMoveRequest): Result<FreshBoxMove>
    suspend fun completeMove(moveId: String): Result<Unit>
    suspend fun scanBox(barcode: String): Result<FreshBoxMove?>
}
