package com.example.twitterclone.data.mapper

import com.example.twitterclone.data.remote.dto.AuthResponse
import com.example.twitterclone.data.remote.dto.TweetDto
import com.example.twitterclone.data.remote.dto.UserDto
import com.example.twitterclone.domain.model.AuthData
import com.example.twitterclone.domain.model.Tweet
import com.example.twitterclone.domain.model.User

fun UserDto.toDomain() = User(
    id = id,
    username = username,
    email = email,
    displayName = displayName,
    bio = bio,
    createdAt = createdAt,
    followersCount = followersCount ?: 0,
    followingCount = followingCount ?: 0
)

fun TweetDto.toDomain() = Tweet(
    id = id,
    content = content,
    userId = userId,
    username = username,
    createdAt = createdAt
)

fun AuthResponse.toDomain() = AuthData(
    token = token,
    user = user.toDomain()
)
