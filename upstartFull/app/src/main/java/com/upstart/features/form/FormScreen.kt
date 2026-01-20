package com.upstart.features.form

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.upstart.features.loanDetails.LoanDetailsScreen
import com.upstart.features.personalInfo.PersonalInfoScreen
import com.upstart.features.result.ResultStepScreen
import com.upstart.core.theme.Spacing
import com.upstart.core.theme.UpstartTheme

@Composable
fun FormScreen(
    coordinator: FormCoordinator
) {
    val formState by coordinator.state.collectAsStateWithLifecycle()
    val loanDetailsState by coordinator.loanDetailsViewModel.state.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = Spacing.xxxl, horizontal = Spacing.lg)
            .verticalScroll(rememberScrollState())
    ) {
        // Header
        Text(
            text = "Loan Application",
            style = UpstartTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = Spacing.sm)
        )

        // Step indicator
        StepIndicator(
            currentStep = formState.currentStep,
            modifier = Modifier.padding(bottom = Spacing.xl)
        )

        // Step content
        when (formState.currentStep) {
            FormStep.LOAN_DETAILS -> {
                LoanDetailsScreen(
                    viewModel = coordinator.loanDetailsViewModel,
                    enabled = !formState.isLoadingStep
                )
            }
            FormStep.PERSONAL_INFO -> {
                PersonalInfoScreen(
                    viewModel = coordinator.personalInfoViewModel,
                    enabled = !formState.isLoadingStep
                )
            }
            FormStep.RESULT -> {
                ResultStepScreen(
                    applicationId = loanDetailsState.applicationId,
                    onStartOver = {
                        coordinator.handleIntent(FormIntent.Reset)
                    }
                )
            }
        }

        // Only show navigation for non-result steps
        if (formState.currentStep != FormStep.RESULT) {
            Spacer(modifier = Modifier.height(Spacing.lg))

            // Error message
            formState.result?.let { res ->
                if (!res.success) {
                    Text(
                        text = res.message,
                        style = UpstartTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(bottom = Spacing.lg)
                    )
                }
            }

            // Navigation buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.md)
            ) {
                // Previous button
                if (formState.canGoBack) {
                    OutlinedButton(
                        onClick = { coordinator.handleIntent(FormIntent.PreviousStep) },
                        enabled = !formState.isLoadingStep,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.primary,
                            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        border = BorderStroke(
                            1.dp,
                            if (!formState.isLoadingStep) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    ) {
                        Text("Previous")
                    }
                }

                // Next or Submit button
                Button(
                    onClick = {
                        if (formState.isLastStep) {
                            coordinator.handleIntent(FormIntent.Submit)
                        } else {
                            coordinator.handleIntent(FormIntent.NextStep)
                        }
                    },
                    enabled = !formState.isLoadingStep && coordinator.canProceed(),
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                ) {
                    if (formState.isLoadingStep) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(Spacing.xl),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text(
                            text = if (formState.isLastStep) "Submit Application" else "Next",
                            style = UpstartTheme.typography.labelLarge
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StepIndicator(
    currentStep: FormStep,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        FormStep.entries.forEachIndexed { index, step ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Step circle
                Box(
                    modifier = Modifier
                        .size(Spacing.xxl)
                        .background(
                            color = if (step == currentStep) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant
                            },
                            shape = RoundedCornerShape(Spacing.xs)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${index + 1}",
                        style = UpstartTheme.typography.bodyMedium,
                        color = if (step == currentStep) {
                            MaterialTheme.colorScheme.onPrimary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }

                // Step label
                Text(
                    text = step.displayName,
                    style = UpstartTheme.typography.labelSmall,
                    color = if (step == currentStep) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    modifier = Modifier.padding(start = Spacing.sm)
                )
            }
        }
    }
}
