package com.example.twitterclone.data.remote.dto

import com.google.gson.annotations.SerializedName

data class LoginRequest(
    val username: String,
    val password: String
)

data class RegisterRequest(
    val username: String,
    val email: String,
    val password: String,
    @SerializedName("display_name") val displayName: String? = null
)

data class AuthResponse(
    @SerializedName("access_token") val token: String,
    val user: UserDto
)

data class UserDto(
    val id: Int,
    val username: String,
    val email: String,
    @SerializedName("display_name") val displayName: String? = null,
    val bio: String? = null,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("followers_count") val followersCount: Int? = 0,
    @SerializedName("following_count") val followingCount: Int? = 0
)

data class TweetDto(
    val id: Int,
    val content: String,
    @SerializedName("user_id") val userId: Int,
    val username: String,
    @SerializedName("created_at") val createdAt: String
)

data class CreateTweetRequest(
    val content: String
)

data class PaginatedResponse<T>(
    val tweets: List<T>? = null,
    val data: List<T>? = null,
    val pagination: Pagination? = null
)

data class Pagination(
    val page: Int,
    @SerializedName("per_page") val perPage: Int,
    @SerializedName("total_pages") val totalPages: Int,
    @SerializedName("total_items") val totalItems: Int
)
