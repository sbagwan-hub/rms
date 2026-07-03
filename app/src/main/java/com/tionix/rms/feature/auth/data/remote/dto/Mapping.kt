package com.tionix.rms.feature.auth.data.remote.dto

import com.tionix.rms.feature.auth.domain.model.LoginRequest
import com.tionix.rms.feature.auth.domain.model.User

fun LoginRequest.toDto(): LoginRequestDto {
    return LoginRequestDto(
        username = username,
        password = password,
        deviceId = deviceId
    )
}

fun UserDto.toDomain(): User {
    return User(
        id = id,
        username = username,
        email = email,
        companyId = companyId,
        deviceId = deviceId
    )
}
