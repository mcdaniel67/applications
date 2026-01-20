package com.upstart.features.personalInfo

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.upstart.core.theme.Spacing
import com.upstart.core.theme.TextFieldDefaults
import com.upstart.core.theme.UpstartTheme
import com.upstart.core.validation.FieldValidation

@Composable
fun PersonalInfoScreen(
    viewModel: PersonalInfoViewModel,
    enabled: Boolean = true
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    PersonalInfoContent(
        state = state,
        onFirstNameChange = { viewModel.handleIntent(PersonalInfoIntent.UpdateFirstName(it)) },
        onLastNameChange = { viewModel.handleIntent(PersonalInfoIntent.UpdateLastName(it)) },
        onEmailChange = { viewModel.handleIntent(PersonalInfoIntent.UpdateEmail(it)) },
        enabled = enabled
    )
}

@Composable
private fun PersonalInfoContent(
    state: PersonalInfoState,
    onFirstNameChange: (String) -> Unit,
    onLastNameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    enabled: Boolean = true
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Spacing.sm),
        verticalArrangement = Arrangement.spacedBy(Spacing.lg)
    ) {
        // First Name
        Column {
            OutlinedTextField(
                value = state.firstName,
                onValueChange = onFirstNameChange,
                label = { Text("First Name") },
                isError = !state.firstNameValidation.isValid,
                modifier = Modifier.fillMaxWidth(),
                enabled = enabled,
                colors = TextFieldDefaults.colors()
            )
            if (!state.firstNameValidation.isValid) {
                Text(
                    text = state.firstNameValidation.errorMessage ?: "",
                    style = UpstartTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = Spacing.xs)
                )
            }
        }

        // Last Name
        Column {
            OutlinedTextField(
                value = state.lastName,
                onValueChange = onLastNameChange,
                label = { Text("Last Name") },
                isError = !state.lastNameValidation.isValid,
                modifier = Modifier.fillMaxWidth(),
                enabled = enabled,
                colors = TextFieldDefaults.colors()
            )
            if (!state.lastNameValidation.isValid) {
                Text(
                    text = state.lastNameValidation.errorMessage ?: "",
                    style = UpstartTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = Spacing.xs)
                )
            }
        }

        // Email
        Column {
            OutlinedTextField(
                value = state.email,
                onValueChange = onEmailChange,
                label = { Text("Email") },
                isError = !state.emailValidation.isValid,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth(),
                enabled = enabled,
                colors = TextFieldDefaults.colors()
            )
            if (!state.emailValidation.isValid) {
                Text(
                    text = state.emailValidation.errorMessage ?: "",
                    style = UpstartTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = Spacing.xs)
                )
            }
        }
    }
}

// Preview Functions
@Preview(name = "Light Mode - Valid State", showBackground = true)
@Composable
private fun PersonalInfoPreview_Light_Valid() {
    UpstartTheme(darkTheme = false) {
        Surface {
            PersonalInfoContent(
                state = PersonalInfoState(
                    firstName = "John",
                    lastName = "Doe",
                    email = "john.doe@example.com",
                    firstNameValidation = FieldValidation.Valid,
                    lastNameValidation = FieldValidation.Valid,
                    emailValidation = FieldValidation.Valid
                ),
                onFirstNameChange = {},
                onLastNameChange = {},
                onEmailChange = {},
                enabled = true
            )
        }
    }
}

@Preview(name = "Light Mode - Invalid State", showBackground = true)
@Composable
private fun PersonalInfoPreview_Light_Invalid() {
    UpstartTheme(darkTheme = false) {
        Surface {
            PersonalInfoContent(
                state = PersonalInfoState(
                    firstName = "J",
                    lastName = "D",
                    email = "invalid-email",
                    firstNameValidation = FieldValidation.Invalid("First name must be at least 2 characters"),
                    lastNameValidation = FieldValidation.Invalid("Last name must be at least 2 characters"),
                    emailValidation = FieldValidation.Invalid("Please enter a valid email address")
                ),
                onFirstNameChange = {},
                onLastNameChange = {},
                onEmailChange = {},
                enabled = true
            )
        }
    }
}

@Preview(name = "Light Mode - Empty State", showBackground = true)
@Composable
private fun PersonalInfoPreview_Light_Empty() {
    UpstartTheme(darkTheme = false) {
        Surface {
            PersonalInfoContent(
                state = PersonalInfoState(),
                onFirstNameChange = {},
                onLastNameChange = {},
                onEmailChange = {},
                enabled = true
            )
        }
    }
}
