package com.tionix.rms.feature.profile.domain.usecase

import com.tionix.rms.feature.profile.domain.model.DailyStats
import com.tionix.rms.feature.profile.domain.model.UserProfile
import com.tionix.rms.feature.profile.domain.repository.ProfileRepository
import javax.inject.Inject

class GetProfileUseCase @Inject constructor(
    private val repository: ProfileRepository
) {
    suspend operator fun invoke(): Result<UserProfile> {
        // TODO: BACKEND ENDPOINT PENDING - DataStore only for now
        return repository.getProfile()
    }
}

class GetDailyStatsUseCase @Inject constructor(
    private val repository: ProfileRepository
) {
    suspend operator fun invoke(): Result<DailyStats> {
        // TODO: BACKEND ENDPOINT PENDING - Room activity log only for now
        return repository.getDailyStats()
    }
}

class GetPendingSyncCountUseCase @Inject constructor(
    private val repository: ProfileRepository
) {
    suspend operator fun invoke(): Result<Int> {
        // TODO: BACKEND ENDPOINT PENDING - Room pending queue only for now
        return repository.getPendingSyncCount()
    }
}

class LogoutUseCase @Inject constructor(
    private val repository: ProfileRepository
) {
    suspend operator fun invoke(): Result<Unit> {
        // TODO: BACKEND ENDPOINT PENDING - Clear DataStore session only for now
        return repository.logout()
    }
}
