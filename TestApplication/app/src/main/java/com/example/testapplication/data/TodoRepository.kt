package com.example.testapplication.data

import com.example.testapplication.domain.models.TodoItemModel
import kotlinx.coroutines.flow.Flow

/**
 * This is a base repository following Google's standard MVVM architecture.
 *
 * We use an interface because we are temporarily substituting a mock instance in place. This allows
 * us to decouple the development of the UI from a potentially in-progress backend.
 */
interface TodoRepository {
    /**
     * Gets the data which we should render to the customer. This is a flowable to promote
     * unidirectional data flow
     */
    suspend fun getTodos(): Flow<List<TodoItemModel>>

    suspend fun toggleTodo(id: String)

    suspend fun addTodo(text: String, checked: Boolean)
}