package com.example.testapplication.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.testapplication.domain.models.TodoItemModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodoScreen(
    uiState: TodoUiState,
    onToggle: (String) -> Unit,
    onAdd: (title: String) -> Unit,
) {
    val snackbarHostState = remember { SnackbarHostState() }

    val (showTodoCreator, toggleTodoCreator) = rememberSaveable { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(),
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Todo App", style = MaterialTheme.typography.titleLarge)
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            if (!showTodoCreator) FloatingActionButton(
                onClick = {
                    toggleTodoCreator(true)
                },
            ) {
                Icon(Icons.Default.AddCircle, contentDescription = "Add todo")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
        ) {
            if (showTodoCreator) {
                TodoInputCard(snackbarHostState, {
                    onAdd(it)
                    toggleTodoCreator(false)
                })
            } else {
                // Input Card
                when (uiState) {
                    is TodoUiState.Error -> ErrorDialog(uiState.message)
                    is TodoUiState.Loading -> LoadingState()
                    is TodoUiState.Ready -> DataLoaded(uiState.todos, onToggle)
                }
            }
        }
    }
}

@Composable
private fun DataLoaded(
    todos: List<TodoItemModel>,
    onToggle: (String) -> Unit
) {
    if (todos.isEmpty()) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text("All To-dos are To-Done")
        }
    } else {
        CardList(todos, onToggle)
    }
}

@Composable
private fun CardList(
    todos: List<TodoItemModel>,
    onToggle: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(9.dp),
        contentPadding = PaddingValues(bottom = 88.dp)
    ) {
        items(todos, key = TodoItemModel::getKey) { todo ->
            TodoCard(todo = todo, onToggle = onToggle)
        }
    }
}

@Composable
private fun TodoCard(
    todo: TodoItemModel,
    onToggle: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            RowWithCheckbox(todo.text, todo.checked, { onToggle(todo.id) })
        }
    }
}

@Composable
private fun TodoInputCard(
    snackbarHostState: SnackbarHostState,
    onAdd: (title: String) -> Unit,
) {
    val validationEvents = remember { MutableSharedFlow<String>(extraBufferCapacity = 1)}
    LaunchedEffect(snackbarHostState) {
        validationEvents.collectLatest { snackbarHostState.showSnackbar(it) }
    }

    val (todo, setTodo) = rememberSaveable { mutableStateOf("") }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Add Todo",
                style = MaterialTheme.typography.titleMedium,
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = todo,
                onValueChange = setTodo,
                label = { Text("Title") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = {
                    if (todo.isBlank()) {
                        validationEvents.tryEmit("Give your todo a todo")
                    } else {
                        // Add and reset the state
                        onAdd(todo)
                        setTodo("")
                    }
                },
                shape = MaterialTheme.shapes.medium,
            ) {
                Icon(Icons.Default.Done, contentDescription = "Save Todo")
            }
        }

    }
}

@Composable
private fun RowWithCheckbox(
    label: String,
    checked: Boolean,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium.copy(
                textDecoration = if (checked) TextDecoration.LineThrough else TextDecoration.None
            ),
            fontWeight = FontWeight.SemiBold
        )
        Checkbox(checked = checked, onCheckedChange = { onToggle() })
    }
}

@Composable
private fun ErrorDialog(
    message: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = modifier.fillMaxWidth()
        ) {
            Icon(
                Icons.Default.Warning,
                contentDescription = "Error Icon",
            )
            Text(
                text = "Something Went Wrong!",
                style = MaterialTheme.typography.titleLarge,
                modifier = modifier.padding(start = 5.dp)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun LoadingState(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator()
    }
}

@Preview(showBackground = true)
@Composable
private fun TodoScreenPreview() {
    TodoScreen(
        uiState = TodoUiState.Ready(
            todos = listOf(
                TodoItemModel("Mow the grass"),
                TodoItemModel("Go grocery shopping!")
            )
        ),
        onToggle = {},
        onAdd = { _ -> }
    )
}

@Preview(showBackground = true)
@Composable
private fun LoadingTodoScreenPreview() {
    TodoScreen(
        uiState = TodoUiState.Loading,
        onToggle = {},
        onAdd = { _ -> }
    )
}

@Preview(showBackground = true)
@Composable
private fun ErrorTodoScreenPreview() {
    TodoScreen(
        uiState = TodoUiState.Error("There was a critical fault in the network layer."),
        onToggle = {},
        onAdd = { _ -> }
    )
}
