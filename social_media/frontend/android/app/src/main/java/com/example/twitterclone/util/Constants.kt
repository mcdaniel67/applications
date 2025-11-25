package com.example.twitterclone.util

import com.example.twitterclone.BuildConfig

object Constants {
    // API - Uses BuildConfig for different environments
    val BASE_URL = BuildConfig.API_BASE_URL
    const val CONNECT_TIMEOUT = 30L
    const val READ_TIMEOUT = 30L
    const val WRITE_TIMEOUT = 30L
    
    // DataStore
    const val DATASTORE_NAME = "twitter_clone_prefs"
    const val KEY_AUTH_TOKEN = "auth_token"
    const val KEY_USER_ID = "user_id"
    const val KEY_USERNAME = "username"
    
    // Pagination
    const val DEFAULT_PAGE_SIZE = 20
    const val MAX_PAGE_SIZE = 100
    
    // Tweet
    const val MAX_TWEET_LENGTH = 280
    
    // Database
    const val DATABASE_NAME = "twitter_clone_db"
}
