package com.example.twitterclone.domain.repository

import com.example.twitterclone.domain.model.Tweet
import com.example.twitterclone.util.Result

interface TweetRepository {
    suspend fun getFeed(page: Int, perPage: Int): Result<List<Tweet>>
    suspend fun getGlobalFeed(page: Int, perPage: Int): Result<List<Tweet>>
}
