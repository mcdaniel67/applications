package com.example.twitterclone.data.repository

import com.example.twitterclone.data.mapper.toDomain
import com.example.twitterclone.data.remote.ApiService
import com.example.twitterclone.domain.model.Tweet
import com.example.twitterclone.domain.repository.TweetRepository
import com.example.twitterclone.util.Result
import javax.inject.Inject

class TweetRepositoryImpl @Inject constructor(
    private val apiService: ApiService
) : TweetRepository {
    
    override suspend fun getFeed(page: Int, perPage: Int): Result<List<Tweet>> {
        return try {
            val response = apiService.getFeed(page, perPage)
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                val tweets = (body.tweets ?: body.data)?.map { it.toDomain() } ?: emptyList()
                Result.Success(tweets)
            } else {
                Result.Error(response.message(), response.code())
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Unknown error")
        }
    }
    
    override suspend fun getGlobalFeed(page: Int, perPage: Int): Result<List<Tweet>> {
        return try {
            val response = apiService.getGlobalFeed(page, perPage)
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                val tweets = (body.tweets ?: body.data)?.map { it.toDomain() } ?: emptyList()
                Result.Success(tweets)
            } else {
                Result.Error(response.message(), response.code())
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Unknown error")
        }
    }
}
