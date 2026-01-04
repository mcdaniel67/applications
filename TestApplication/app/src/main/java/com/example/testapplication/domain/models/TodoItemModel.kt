package com.example.testapplication.domain.models

import java.util.UUID

/**
 * This is a model which represents an item which needs done.
 */
data class TodoItemModel(
    val text: String,
    val checked: Boolean = false,
    val hidden: Boolean = false,
    val id: String = UUID.randomUUID().toString(),
) {
    fun getKey() = id.hashCode()
}