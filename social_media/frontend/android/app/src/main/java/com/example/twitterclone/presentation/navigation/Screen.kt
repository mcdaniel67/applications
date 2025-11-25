package com.example.twitterclone.presentation.navigation

sealed class Screen(val route: String) {
    data object Login : Screen("login")
    data object Register : Screen("register")
    data object Feed : Screen("feed")
    data object Profile : Screen("profile/{userId}") {
        fun createRoute(userId: Int) = "profile/$userId"
    }
    data object CreateTweet : Screen("create_tweet")
}
