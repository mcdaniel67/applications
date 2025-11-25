package com.example.twitterclone.domain.repository

import com.example.twitterclone.domain.model.AuthData
import com.example.twitterclone.util.Result
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    suspend fun login(username: String, password: String): Result<AuthData>
    suspend fun register(username: String, email: String, password: String, displayName: String?): Result<AuthData>
    suspend fun logout(): Result<Unit>
    fun getAuthToken(): Flow<String?>
    fun getUserId(): Flow<Int?>
    fun getUsername(): Flow<String?>
}
