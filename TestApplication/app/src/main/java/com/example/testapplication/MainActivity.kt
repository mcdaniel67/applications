package com.example.testapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.testapplication.ui.TodoScreen
import com.example.testapplication.ui.TodoViewModel
import com.example.testapplication.ui.theme.TestApplicationTheme
import java.util.UUID

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TestApplicationTheme {
                TodoRoute()
            }
        }
    }
}

@Composable
fun TodoRoute(
    viewModel: TodoViewModel = viewModel(factory = TodoViewModel.Factory)
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val onToggle = remember<(String) -> Unit> {
        { viewModel.onTodoToggled(it) }
    }
    val onAdd = remember<(String) -> Unit> {
        { viewModel.addQuickTodo(it, false) }
    }

    TodoScreen(
        uiState = uiState,
        onToggle = onToggle,
        onAdd = onAdd,
    )
}
