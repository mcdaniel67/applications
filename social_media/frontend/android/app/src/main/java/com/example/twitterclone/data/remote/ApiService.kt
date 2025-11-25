package com.example.twitterclone.data.remote

import com.example.twitterclone.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    
    // Auth
    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>
    
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>
    
    @POST("auth/logout")
    suspend fun logout(): Response<Unit>
    
    // Users
    @GET("users/{id}")
    suspend fun getUser(@Path("id") userId: Int): Response<UserDto>
    
    @GET("users")
    suspend fun getUsers(
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 20
    ): Response<PaginatedResponse<UserDto>>
    
    @PUT("users/{id}")
    suspend fun updateUser(
        @Path("id") userId: Int,
        @Body user: Map<String, String>
    ): Response<UserDto>
    
    // Tweets
    @GET("tweets")
    suspend fun getTweets(
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 20,
        @Query("sort") sort: String = "newest"
    ): Response<PaginatedResponse<TweetDto>>
    
    @GET("tweets/{id}")
    suspend fun getTweet(@Path("id") tweetId: Int): Response<TweetDto>
    
    @POST("tweets")
    suspend fun createTweet(@Body request: CreateTweetRequest): Response<TweetDto>
    
    @PUT("tweets/{id}")
    suspend fun updateTweet(
        @Path("id") tweetId: Int,
        @Body request: CreateTweetRequest
    ): Response<TweetDto>
    
    @DELETE("tweets/{id}")
    suspend fun deleteTweet(@Path("id") tweetId: Int): Response<Unit>
    
    @GET("users/{id}/tweets")
    suspend fun getUserTweets(
        @Path("id") userId: Int,
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 20
    ): Response<PaginatedResponse<TweetDto>>
    
    // Feed
    @GET("feed")
    suspend fun getFeed(
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 20
    ): Response<PaginatedResponse<TweetDto>>
    
    @GET("feed/global")
    suspend fun getGlobalFeed(
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 20
    ): Response<PaginatedResponse<TweetDto>>
    
    // Follow
    @POST("users/{id}/follow")
    suspend fun followUser(@Path("id") userId: Int): Response<Unit>
    
    @DELETE("users/{id}/follow")
    suspend fun unfollowUser(@Path("id") userId: Int): Response<Unit>
    
    @GET("users/{id}/followers")
    suspend fun getFollowers(
        @Path("id") userId: Int,
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 20
    ): Response<PaginatedResponse<UserDto>>
    
    @GET("users/{id}/following")
    suspend fun getFollowing(
        @Path("id") userId: Int,
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 20
    ): Response<PaginatedResponse<UserDto>>
}
