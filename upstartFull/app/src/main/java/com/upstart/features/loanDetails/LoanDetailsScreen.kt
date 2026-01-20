package com.upstart.features.loanDetails

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.upstart.core.config.FormConfig
import com.upstart.core.models.LoanType
import com.upstart.core.theme.Spacing
import com.upstart.core.theme.TextFieldDefaults
import com.upstart.core.theme.UpstartTheme
import com.upstart.core.utils.CurrencyFormatter
import com.upstart.core.validation.FieldValidation

@Composable
fun LoanDetailsScreen(
    viewModel: LoanDetailsViewModel,
    enabled: Boolean = true
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LoanDetailsContent(
        state = state,
        onAmountChange = { viewModel.handleIntent(LoanDetailsIntent.UpdateAmount(it)) },
        onLoanTypeSelected = { viewModel.handleIntent(LoanDetailsIntent.UpdateLoanType(it)) },
        enabled = enabled
    )
}

@Composable
private fun LoanDetailsContent(
    state: LoanDetailsState,
    onAmountChange: (Float) -> Unit,
    onLoanTypeSelected: (LoanType) -> Unit,
    enabled: Boolean = true
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Spacing.sm)
    ) {
        // Loan Type Dropdown
        Column(
            modifier = Modifier.padding(bottom = Spacing.lg)
        ) {
            LoanTypeDropdown(
                selectedLoanType = state.loanType,
                onLoanTypeSelected = onLoanTypeSelected,
                enabled = enabled
            )
            if (!state.loanTypeValidation.isValid) {
                Text(
                    text = state.loanTypeValidation.errorMessage ?: "",
                    style = UpstartTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = Spacing.xs)
                )
            }
        }

        // Loan Amount Slider
        Column {
            Text(
                text = "Loan Amount",
                style = UpstartTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = Spacing.sm)
            )
            Text(
                text = CurrencyFormatter.formatCurrency(state.amount),
                style = UpstartTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = Spacing.sm)
            )
            Slider(
                value = state.amount,
                onValueChange = onAmountChange,
                valueRange = FormConfig.MIN_LOAN_AMOUNT..FormConfig.MAX_LOAN_AMOUNT,
                steps = FormConfig.LOAN_SLIDER_STEPS,
                enabled = enabled,
                modifier = Modifier.fillMaxWidth(),
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.primary,
                    activeTrackColor = MaterialTheme.colorScheme.primary,
                    activeTickColor = MaterialTheme.colorScheme.primary,
                    inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant,
                    inactiveTickColor = MaterialTheme.colorScheme.surfaceVariant,
                    disabledThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    disabledActiveTrackColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    disabledInactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = CurrencyFormatter.formatCurrency(FormConfig.MIN_LOAN_AMOUNT),
                    style = UpstartTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = CurrencyFormatter.formatCurrency(FormConfig.MAX_LOAN_AMOUNT),
                    style = UpstartTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (!state.amountValidation.isValid) {
                Text(
                    text = state.amountValidation.errorMessage ?: "",
                    style = UpstartTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = Spacing.xs)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LoanTypeDropdown(
    selectedLoanType: LoanType?,
    onLoanTypeSelected: (LoanType) -> Unit,
    enabled: Boolean
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded && enabled }
    ) {
        OutlinedTextField(
            value = selectedLoanType?.displayName ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text("Loan Type") },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            colors = TextFieldDefaults.colors(),
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
            enabled = enabled
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            LoanType.entries.forEach { loanType ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = loanType.displayName,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    },
                    onClick = {
                        onLoanTypeSelected(loanType)
                        expanded = false
                    },
                    colors = MenuDefaults.itemColors(
                        textColor = MaterialTheme.colorScheme.onSurface
                    )
                )
            }
        }
    }
}

// Preview Functions
@Preview(name = "Light Mode - Valid State", showBackground = true)
@Composable
private fun LoanDetailsPreview_Light_Valid() {
    UpstartTheme(darkTheme = false) {
        Surface {
            LoanDetailsContent(
                state = LoanDetailsState(
                    amount = 15000f,
                    loanType = LoanType.PERSONAL,
                    amountValidation = FieldValidation.Valid,
                    loanTypeValidation = FieldValidation.Valid
                ),
                onAmountChange = {},
                onLoanTypeSelected = {},
                enabled = true
            )
        }
    }
}

@Preview(name = "Light Mode - Invalid State", showBackground = true)
@Composable
private fun LoanDetailsPreview_Light_Invalid() {
    UpstartTheme(darkTheme = false) {
        Surface {
            LoanDetailsContent(
                state = LoanDetailsState(
                    amount = 500f,
                    loanType = null,
                    amountValidation = FieldValidation.Invalid("Loan amount must be at least \$1,000"),
                    loanTypeValidation = FieldValidation.Invalid("Please select a loan type")
                ),
                onAmountChange = {},
                onLoanTypeSelected = {},
                enabled = true
            )
        }
    }
}

@Preview(name = "Light Mode - Disabled State", showBackground = true)
@Composable
private fun LoanDetailsPreview_Light_Disabled() {
    UpstartTheme(darkTheme = false) {
        Surface {
            LoanDetailsContent(
                state = LoanDetailsState(
                    amount = 10000f,
                    loanType = LoanType.HOME_IMPROVEMENT,
                    amountValidation = FieldValidation.Valid,
                    loanTypeValidation = FieldValidation.Valid
                ),
                onAmountChange = {},
                onLoanTypeSelected = {},
                enabled = false
            )
        }
    }
}
