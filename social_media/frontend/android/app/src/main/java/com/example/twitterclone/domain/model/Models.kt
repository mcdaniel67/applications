package com.example.twitterclone.domain.model

data class User(
    val id: Int,
    val username: String,
    val email: String,
    val displayName: String?,
    val bio: String?,
    val createdAt: String,
    val followersCount: Int = 0,
    val followingCount: Int = 0
)

data class Tweet(
    val id: Int,
    val content: String,
    val userId: Int,
    val username: String,
    val createdAt: String
)

data class AuthData(
    val token: String,
    val user: User
)
