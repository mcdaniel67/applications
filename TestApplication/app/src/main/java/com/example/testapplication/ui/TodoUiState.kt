package com.example.testapplication.ui

import com.example.testapplication.domain.models.TodoItemModel

/**
 * Basic sealed interface which helps us to represent the state of our UI.
 *
 * We use [Loading] when we are waiting for the UI to load, which is helpful for displaying skeleton.
 *
 * We use [Ready] when we have data ready to display
 *
 * We use [Error] when there's been an error downstream to help allow customers to retry.
 */
sealed interface TodoUiState {
    data object Loading : TodoUiState

    data class Ready(
        val todos: List<TodoItemModel>,
    ) : TodoUiState

    data class Error(val message: String) : TodoUiState
}