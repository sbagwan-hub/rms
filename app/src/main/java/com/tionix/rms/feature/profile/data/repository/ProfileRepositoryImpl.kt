package com.tionix.rms.feature.profile.data.repository

import com.tionix.rms.feature.profile.domain.model.DailyStats
import com.tionix.rms.feature.profile.domain.model.UserProfile
import com.tionix.rms.feature.profile.domain.repository.ProfileRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProfileRepositoryImpl @Inject constructor() : ProfileRepository {
    override suspend fun getProfile(): Result<UserProfile> {
        return Result.success(
            UserProfile(
                id = "current_user",
                name = "Avinash Magar",
                employeeId = "EMP-001",
                role = "WAREHOUSE_OPERATOR",
                warehouseId = "WH-001",
                warehouseName = "Central Warehouse",
                siteId = "SITE-001",
                siteName = "Main Site",
                email = "avinash@tionix.com"
            )
        )
    }

    override suspend fun getDailyStats(): Result<DailyStats> {
        return Result.success(
            DailyStats(
                freshBoxMoves = 5,
                refiles = 8,
                transfers = 3,
                segregations = 2,
                merges = 1,
                verifications = 4,
                totalActions = 23
            )
        )
    }

    override suspend fun getPendingSyncCount(): Result<Int> {
        return Result.success(0)
    }

    override suspend fun logout(): Result<Unit> {
        return Result.success(Unit)
    }
}
