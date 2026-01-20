package com.upstart.features.result

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.upstart.core.models.LoanResponse
import com.upstart.core.theme.Spacing
import com.upstart.core.theme.UpstartTheme

@Composable
fun ResultStepScreen(
    applicationId: String?,
    onStartOver: () -> Unit,
    viewModel: ResultViewModel = viewModel(key = applicationId)
) {
    val state by viewModel.state.collectAsState()

    // Fetch application status when this screen is displayed
    LaunchedEffect(applicationId) {
        viewModel.fetchApplicationStatus(applicationId)
    }

    ResultStepContent(
        state = state,
        onStartOver = onStartOver
    )
}

@Composable
private fun ResultStepContent(
    state: ResultState,
    onStartOver: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Spacing.xxl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        when (state) {
            is ResultState.Loading -> {
                CircularProgressIndicator(
                    modifier = Modifier
                        .size(48.dp)
                        .padding(bottom = Spacing.xxl),
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.size(Spacing.xl))
                Text(
                    text = "Fetching application status...",
                    style = UpstartTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            is ResultState.Success -> {
                val result = state.result
                Text(
                    text = if (result.success) "✓" else "✗",
                    style = UpstartTheme.typography.displayLarge,
                    color = if (result.success) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.error
                    },
                    modifier = Modifier.padding(bottom = Spacing.xl)
                )

                Text(
                    text = result.message,
                    style = UpstartTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = Spacing.lg)
                )

                result.applicationId?.let { appId ->
                    Text(
                        text = "Application ID: $appId",
                        style = UpstartTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = Spacing.xxl)
                    )
                }

                Button(
                    onClick = onStartOver,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text("Start Over")
                }
            }

            is ResultState.Error -> {
                val errorMessage = state.message
                Text(
                    text = "✗",
                    style = UpstartTheme.typography.displayLarge,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(bottom = Spacing.xl)
                )

                Text(
                    text = errorMessage,
                    style = UpstartTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(bottom = Spacing.xxl)
                )

                Button(
                    onClick = onStartOver,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text("Start Over")
                }
            }
        }
    }
}

// Preview Functions
@Preview(name = "Light Mode - Loading", showBackground = true)
@Composable
private fun ResultPreview_Light_Loading() {
    UpstartTheme(darkTheme = false) {
        Surface {
            ResultStepContent(
                state = ResultState.Loading,
                onStartOver = {}
            )
        }
    }
}

@Preview(name = "Light Mode - Success", showBackground = true)
@Composable
private fun ResultPreview_Light_Success() {
    UpstartTheme(darkTheme = false) {
        Surface {
            ResultStepContent(
                state = ResultState.Success(
                    LoanResponse(
                        success = true,
                        message = "Your loan application has been submitted successfully!",
                        applicationId = "APP-1234567890"
                    )
                ),
                onStartOver = {}
            )
        }
    }
}

@Preview(name = "Light Mode - Failure", showBackground = true)
@Composable
private fun ResultPreview_Light_Failure() {
    UpstartTheme(darkTheme = false) {
        Surface {
            ResultStepContent(
                state = ResultState.Success(
                    LoanResponse(
                        success = false,
                        message = "We're unable to process your application at this time. Please try again later.",
                        applicationId = null
                    )
                ),
                onStartOver = {}
            )
        }
    }
}

@Preview(name = "Light Mode - Error", showBackground = true)
@Composable
private fun ResultPreview_Light_Error() {
    UpstartTheme(darkTheme = false) {
        Surface {
            ResultStepContent(
                state = ResultState.Error("Network error: Failed to fetch application status"),
                onStartOver = {}
            )
        }
    }
}

@Preview(name = "Light Mode - No Application ID", showBackground = true)
@Composable
private fun ResultPreview_Light_NoAppId() {
    UpstartTheme(darkTheme = false) {
        Surface {
            ResultStepContent(
                state = ResultState.Error("No application ID found"),
                onStartOver = {}
            )
        }
    }
}
