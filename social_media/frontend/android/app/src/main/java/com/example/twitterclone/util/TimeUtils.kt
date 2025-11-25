package com.example.twitterclone.util

import java.time.Duration
import java.time.Instant
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

fun formatTimeAgo(timestamp: String): String {
    return try {
        val dateTime = ZonedDateTime.parse(timestamp, DateTimeFormatter.ISO_DATE_TIME)
        val now = Instant.now()
        val then = dateTime.toInstant()
        val duration = Duration.between(then, now)
        
        when {
            duration.toMinutes() < 1 -> "just now"
            duration.toMinutes() < 60 -> "${duration.toMinutes()}m ago"
            duration.toHours() < 24 -> "${duration.toHours()}h ago"
            duration.toDays() < 7 -> "${duration.toDays()}d ago"
            duration.toDays() < 30 -> "${duration.toDays() / 7}w ago"
            else -> "${duration.toDays() / 30}mo ago"
        }
    } catch (e: Exception) {
        timestamp
    }
}
