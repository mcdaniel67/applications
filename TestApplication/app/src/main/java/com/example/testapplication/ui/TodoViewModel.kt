package com.example.testapplication.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.testapplication.data.MockTodoRepository
import com.example.testapplication.data.TodoRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TodoViewModel(
    private val repository: TodoRepository = MockTodoRepository()
): ViewModel() {

    private val _uiState = MutableStateFlow<TodoUiState>(TodoUiState.Loading)
    val uiState: StateFlow<TodoUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                repository.getTodos()
                    .catch { throwable ->
                        _uiState.update { TodoUiState.Error("Error! Throwable ${throwable.toString()}") }
                    }
                    .collect { todos ->
                        _uiState.update { TodoUiState.Ready(todos.mapNotNull { if (it.hidden) null else it }) }
                    }
            }
        }
    }

    fun onTodoToggled(id: String) = viewModelScope.launch { repository.toggleTodo(id) }

    fun addQuickTodo(title: String, checked: Boolean) = viewModelScope.launch {
        _uiState.update { TodoUiState.Loading }
        repository.addTodo(title.trim(), checked)
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                TodoViewModel()
            }
        }
    }
}