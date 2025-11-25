package com.example.twitterclone.data.repository

import com.example.twitterclone.data.local.PreferencesManager
import com.example.twitterclone.data.mapper.toDomain
import com.example.twitterclone.data.remote.ApiService
import com.example.twitterclone.data.remote.dto.LoginRequest
import com.example.twitterclone.data.remote.dto.RegisterRequest
import com.example.twitterclone.domain.model.AuthData
import com.example.twitterclone.domain.repository.AuthRepository
import com.example.twitterclone.util.Result
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    private val preferencesManager: PreferencesManager
) : AuthRepository {
    
    override suspend fun login(username: String, password: String): Result<AuthData> {
        return try {
            val response = apiService.login(LoginRequest(username, password))
            if (response.isSuccessful && response.body() != null) {
                val authData = response.body()!!.toDomain()
                preferencesManager.saveAuthData(
                    authData.token,
                    authData.user.id,
                    authData.user.username
                )
                Result.Success(authData)
            } else {
                Result.Error(response.message(), response.code())
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Unknown error")
        }
    }
    
    override suspend fun register(
        username: String,
        email: String,
        password: String,
        displayName: String?
    ): Result<AuthData> {
        return try {
            val response = apiService.register(
                RegisterRequest(username, email, password, displayName)
            )
            if (response.isSuccessful && response.body() != null) {
                val authData = response.body()!!.toDomain()
                preferencesManager.saveAuthData(
                    authData.token,
                    authData.user.id,
                    authData.user.username
                )
                Result.Success(authData)
            } else {
                Result.Error(response.message(), response.code())
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Unknown error")
        }
    }
    
    override suspend fun logout(): Result<Unit> {
        return try {
            apiService.logout()
            preferencesManager.clearAuthData()
            Result.Success(Unit)
        } catch (e: Exception) {
            preferencesManager.clearAuthData()
            Result.Error(e.message ?: "Unknown error")
        }
    }
    
    override fun getAuthToken(): Flow<String?> = preferencesManager.authToken
    
    override fun getUserId(): Flow<Int?> = preferencesManager.userId
    
    override fun getUsername(): Flow<String?> = preferencesManager.username
}
