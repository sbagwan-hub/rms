package com.tionix.rms.feature.profile.domain.usecase

import com.tionix.rms.feature.profile.domain.model.UserProfile
import com.tionix.rms.feature.profile.domain.repository.ProfileRepository
import javax.inject.Inject

class GetProfileUseCase @Inject constructor(
    private val repository: ProfileRepository
) {
    suspend operator fun invoke(): Result<UserProfile> = repository.getProfile()
}

class GetPendingSyncCountUseCase @Inject constructor(
    private val repository: ProfileRepository
) {
    suspend operator fun invoke(): Result<Int> = repository.getPendingSyncCount()
}

class LogoutUseCase @Inject constructor(
    private val repository: ProfileRepository
) {
    suspend operator fun invoke(): Result<Unit> = repository.logout()
}
