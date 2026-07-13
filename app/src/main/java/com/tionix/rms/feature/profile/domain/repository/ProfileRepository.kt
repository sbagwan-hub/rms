package com.tionix.rms.feature.profile.domain.repository

import com.tionix.rms.feature.profile.domain.model.DailyStats
import com.tionix.rms.feature.profile.domain.model.UserProfile

interface ProfileRepository {
    suspend fun getProfile(): Result<UserProfile>
    suspend fun getDailyStats(): Result<DailyStats>
    suspend fun getPendingSyncCount(): Result<Int>
    suspend fun logout(): Result<Unit>
}
