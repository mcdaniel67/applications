package com.example.twitterclone.di

import com.example.twitterclone.data.repository.AuthRepositoryImpl
import com.example.twitterclone.data.repository.TweetRepositoryImpl
import com.example.twitterclone.domain.repository.AuthRepository
import com.example.twitterclone.domain.repository.TweetRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    
    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        authRepositoryImpl: AuthRepositoryImpl
    ): AuthRepository
    
    @Binds
    @Singleton
    abstract fun bindTweetRepository(
        tweetRepositoryImpl: TweetRepositoryImpl
    ): TweetRepository
}
