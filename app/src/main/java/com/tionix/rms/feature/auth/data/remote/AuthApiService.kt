package com.tionix.rms.feature.auth.data.remote

import com.tionix.rms.feature.auth.data.remote.dto.LoginRequestDto
import com.tionix.rms.feature.auth.data.remote.dto.LoginResponseDto
import com.tionix.rms.feature.auth.data.remote.dto.RefreshRequestDto
import com.tionix.rms.feature.auth.data.remote.dto.RefreshResponseDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApiService {
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequestDto): Response<LoginResponseDto>

    @POST("auth/refresh")
    suspend fun refreshToken(@Body request: RefreshRequestDto): Response<RefreshResponseDto>
}
