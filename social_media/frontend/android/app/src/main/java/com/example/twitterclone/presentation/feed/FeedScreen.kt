package com.example.twitterclone.presentation.feed

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.twitterclone.domain.model.Tweet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedScreen(
    onLogout: () -> Unit,
    viewModel: FeedViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    
    LaunchedEffect(state.error) {
        state.error?.let { error ->
            snackbarHostState.showSnackbar(error)
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Twitter Clone") },
                actions = {
                    IconButton(onClick = {
                        viewModel.logout()
                        onLogout()
                    }) {
                        Icon(Icons.Default.ExitToApp, "Logout")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            TabRow(selectedTabIndex = if (state.isGlobalFeed) 0 else 1) {
                Tab(
                    selected = state.isGlobalFeed,
                    onClick = { viewModel.switchFeed(true) },
                    text = { Text("Global") }
                )
                Tab(
                    selected = !state.isGlobalFeed,
                    onClick = { viewModel.switchFeed(false) },
                    text = { Text("Following") }
                )
            }
            
            if (state.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (state.tweets.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        if (state.isGlobalFeed) "No tweets yet"
                        else "Follow some users to see their tweets"
                    )
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(state.tweets) { tweet ->
                        TweetItem(tweet)
                        Divider()
                    }
                }
            }
        }
    }
}

@Composable
fun TweetItem(tweet: Tweet) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Text(
                text = "@${tweet.username}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = com.example.twitterclone.util.formatTimeAgo(tweet.createdAt),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = tweet.content,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}
