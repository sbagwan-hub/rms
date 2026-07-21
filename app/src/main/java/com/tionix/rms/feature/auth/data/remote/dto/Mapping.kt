package com.tionix.rms.feature.auth.data.remote.dto

import com.tionix.rms.feature.auth.domain.model.LoginRequest
import com.tionix.rms.feature.auth.domain.model.User

import com.tionix.rms.feature.auth.domain.model.UserRole

fun LoginRequest.toDto(): LoginRequestDto {
    return LoginRequestDto(
        username = username,
        password = password,
        device = DeviceInfoDto(
            serialNumber = device.serialNumber,
            model = device.model,
            appVersion = device.appVersion
        )
    )
}

fun UserDto.toDomain(): User {
    val userRole = try {
        UserRole.valueOf(role)
    } catch (e: IllegalArgumentException) {
        UserRole.OPERATOR
    }
    return User(
        id = id,
        fullName = fullName,
        email = email,
        role = userRole
    )
}
