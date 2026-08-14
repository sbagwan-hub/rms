package com.tionix.rms.feature.auth.data.remote

import com.tionix.rms.feature.auth.data.remote.dto.LoginRequestDto
import com.tionix.rms.feature.auth.data.remote.dto.LoginResponseDto
import com.tionix.rms.feature.auth.data.remote.dto.LogoutRequestDto
import com.tionix.rms.feature.auth.data.remote.dto.MeResponseDto
import com.tionix.rms.feature.auth.data.remote.dto.RefreshRequestDto
import com.tionix.rms.feature.auth.data.remote.dto.RefreshResponseDto
import com.tionix.rms.feature.auth.data.remote.dto.SwitchBranchRequestDto
import com.tionix.rms.feature.auth.data.remote.dto.SwitchCompanyRequestDto
import com.tionix.rms.feature.auth.data.remote.dto.SwitchWarehouseRequestDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface AuthApiService {
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequestDto): Response<LoginResponseDto>

    @POST("auth/refresh")
    suspend fun refreshToken(@Body request: RefreshRequestDto): Response<RefreshResponseDto>

    @GET("auth/me")
    suspend fun getMe(): Response<MeResponseDto>

    @POST("auth/logout")
    suspend fun logout(@Body request: LogoutRequestDto): Response<Unit>

    @POST("auth/switch-warehouse")
    suspend fun switchWarehouse(@Body request: SwitchWarehouseRequestDto): Response<LoginResponseDto>

    @POST("auth/switch-branch")
    suspend fun switchBranch(@Body request: SwitchBranchRequestDto): Response<LoginResponseDto>

    @POST("auth/switch-company")
    suspend fun switchCompany(@Body request: SwitchCompanyRequestDto): Response<LoginResponseDto>
}
