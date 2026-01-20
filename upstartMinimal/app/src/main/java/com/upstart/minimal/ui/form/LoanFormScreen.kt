package com.upstart.minimal.ui.form

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Slider
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.upstart.minimal.domain.model.LoanPurpose
import java.text.NumberFormat
import kotlin.math.roundToInt

@Composable
fun LoanFormRoute(
    viewModel: LoanFormViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsState()

    LoanFormScreen(
        state = state,
        onEvent = viewModel::onEvent,
        modifier = modifier
    )
}

@Composable
fun LoanFormScreen(
    state: LoanFormState,
    onEvent: (LoanFormEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize()
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            LoanFormContent(
                state = state,
                onEvent = onEvent,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(horizontal = 24.dp, vertical = 16.dp)
            )

            if (state.isSubmitting) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                )
            }
        }
    }

    state.submissionResult?.let { result ->
        AlertDialog(
            onDismissRequest = { onEvent(LoanFormEvent.DismissResult) },
            title = {
                Text(
                    text = if (result.approved) "Submission Sent" else "Needs Review",
                    style = MaterialTheme.typography.titleLarge
                )
            },
            text = {
                Column {
                    Text(text = result.message)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Reference: ${result.referenceId}",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { onEvent(LoanFormEvent.DismissResult) }) {
                    Text("OK")
                }
            }
        )
    }
}

@Composable
private fun LoanFormContent(
    state: LoanFormState,
    onEvent: (LoanFormEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Loan Request",
            style = MaterialTheme.typography.headlineSmall
        )
        Text(
            text = "Complete the information below to get a fast lending decision.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Surface(
            tonalElevation = 2.dp,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                PurposeDropdown(
                    purposes = state.purposes,
                    selectedPurposeId = state.selectedPurposeId,
                    onPurposeSelected = { onEvent(LoanFormEvent.PurposeSelected(it)) }
                )

                LoanAmountSlider(
                    amount = state.loanAmount,
                    min = state.minAmount,
                    max = state.maxAmount,
                    step = state.amountStep,
                    onAmountChanged = { onEvent(LoanFormEvent.LoanAmountChanged(it)) }
                )

                OutlinedTextField(
                    value = state.borrowerName,
                    onValueChange = { onEvent(LoanFormEvent.BorrowerNameChanged(it)) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Borrower Name") },
                    isError = state.borrowerNameError != null,
                    supportingText = state.borrowerNameError?.let { error ->
                        { Text(error, color = MaterialTheme.colorScheme.error) }
                    }
                )
            }
        }

        state.errorMessage?.let { message ->
            Surface(
                color = MaterialTheme.colorScheme.errorContainer,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = message,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }

        val isSubmitEnabled = state.borrowerName.isNotBlank() &&
            state.selectedPurposeId != null &&
            !state.isSubmitting

        Button(
            modifier = Modifier.fillMaxWidth(),
            enabled = isSubmitEnabled,
            onClick = { onEvent(LoanFormEvent.Submit) }
        ) {
            Text("Submit Request")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PurposeDropdown(
    purposes: List<LoanPurpose>,
    selectedPurposeId: String?,
    onPurposeSelected: (String) -> Unit
) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    val selectedPurpose = purposes.firstOrNull { it.id == selectedPurposeId }
    val labelText = when {
        selectedPurpose != null -> selectedPurpose.label
        purposes.isEmpty() -> "No purposes available"
        else -> "Select a purpose"
    }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = labelText,
            onValueChange = {},
            readOnly = true,
            label = { Text("Purpose") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.exposedDropdownSize()
        ) {
            purposes.forEach { purpose ->
                DropdownMenuItem(
                    text = { Text(purpose.label) },
                    onClick = {
                        expanded = false
                        onPurposeSelected(purpose.id)
                    }
                )
            }
        }
    }
}

@Composable
private fun LoanAmountSlider(
    amount: Float,
    min: Int,
    max: Int,
    step: Int,
    onAmountChanged: (Float) -> Unit
) {
    val currencyFormatter = remember { NumberFormat.getCurrencyInstance() }
    val rawSteps = (max - min) / step
    val steps = rawSteps.coerceAtLeast(1) - 1
    val displayAmount = amount.roundToInt()
    Text(
        text = "Amount: ${currencyFormatter.format(displayAmount)}",
        style = MaterialTheme.typography.titleMedium
    )
    Slider(
        value = amount,
        onValueChange = onAmountChanged,
        valueRange = min.toFloat()..max.toFloat(),
        steps = steps
    )
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = currencyFormatter.format(min))
        Text(text = currencyFormatter.format(max))
    }
}
