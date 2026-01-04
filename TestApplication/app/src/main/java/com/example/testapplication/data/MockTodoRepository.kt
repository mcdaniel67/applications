package com.example.testapplication.data

import com.example.testapplication.domain.models.TodoItemModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.concurrent.TimeUnit
import kotlin.time.Duration.Companion.seconds

/**
 * Mocked repository instance which is backed by an in-memory data store.
 */
class MockTodoRepository(): TodoRepository {
    private var todoList = MutableStateFlow(SEED_TODOS)

    override suspend fun getTodos(): Flow<List<TodoItemModel>> {
        Thread.sleep(TimeUnit.SECONDS.toMillis(30))
        return todoList.asStateFlow()
    }

    override suspend fun toggleTodo(id: String) {
        // Little delay to make the UI look better and emulate network call.
        delay(250)
        var isChecking = false

        todoList.update { todoList ->
            todoList.map { if (it.id == id) {
                if (!it.checked) {
                    isChecking = true
                }

                it.copy(checked = !it.checked)
            } else {
                it
            }}
        }

        if (isChecking) {
            delay(1000L)
            todoList.update { todoList ->
                todoList.map { if (it.id == id) it.copy(hidden = true) else it }
            }
        }
    }

    override suspend fun addTodo(text: String, checked: Boolean) {
        delay(250)
        val newTodo = TodoItemModel(
            text = text,
            checked = checked,
        )

        todoList.update { todoList -> todoList + newTodo }
    }

    companion object {
        private val SEED_TODOS = listOf<TodoItemModel>(
            TodoItemModel(text = "Do the dishes"),
            TodoItemModel(text = "Take out the trash"),
            TodoItemModel(text = "Mow the yard"),
            TodoItemModel(text = "Build Skateboard", checked = true),
            TodoItemModel(text = "Walk the dog"),
        )
    }
}