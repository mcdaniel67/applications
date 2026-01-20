# Adding a New Page to the Multi-Step Form

This guide walks through adding a new step to the loan application form. We'll use **Employment Information** as an example.

---

## Step 1: Create Feature Directory Structure

Create a new feature module under `features/`:

```bash
mkdir -p app/src/main/java/com/example/androidpractice/features/employmentInfo
```

Your feature will contain 3 files:
- `EmploymentInfoState.kt` - Data model and intents
- `EmploymentInfoViewModel.kt` - Business logic and validation
- `EmploymentInfoScreen.kt` - UI components

---

## Step 2: Define State and Intents

Create `features/employmentInfo/EmploymentInfoState.kt`:

```kotlin
package com.example.androidpractice.features.employmentInfo

import com.example.androidpractice.core.validation.FieldValidation

// UI state for employment info step
data class EmploymentInfoState(
    val employerName: String = "",
    val jobTitle: String = "",
    val yearsEmployed: Float = 1f,  // Slider 0-20
    val employerNameValidation: FieldValidation = FieldValidation.Valid,
    val jobTitleValidation: FieldValidation = FieldValidation.Valid,
    val yearsEmployedValidation: FieldValidation = FieldValidation.Valid
) {
    val isValid: Boolean
        get() = employerNameValidation.isValid &&
                jobTitleValidation.isValid &&
                yearsEmployedValidation.isValid
}

// User intents for employment info
sealed class EmploymentInfoIntent {
    data class UpdateEmployerName(val name: String) : EmploymentInfoIntent()
    data class UpdateJobTitle(val title: String) : EmploymentInfoIntent()
    data class UpdateYearsEmployed(val years: Float) : EmploymentInfoIntent()
}
```

**Key points:**
- Each field has a validation state
- `isValid` computed property checks all validations
- Intents represent user actions

---

## Step 3: Create ViewModel with Validation

Create `features/employmentInfo/EmploymentInfoViewModel.kt`:

```kotlin
package com.example.androidpractice.features.employmentInfo

import androidx.lifecycle.ViewModel
import com.example.androidpractice.core.validation.Validators
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class EmploymentInfoViewModel : ViewModel() {

    private val _state = MutableStateFlow(EmploymentInfoState())
    val state: StateFlow<EmploymentInfoState> = _state.asStateFlow()

    fun handleIntent(intent: EmploymentInfoIntent) {
        when (intent) {
            is EmploymentInfoIntent.UpdateEmployerName -> updateEmployerName(intent.name)
            is EmploymentInfoIntent.UpdateJobTitle -> updateJobTitle(intent.title)
            is EmploymentInfoIntent.UpdateYearsEmployed -> updateYearsEmployed(intent.years)
        }
    }

    private fun updateEmployerName(name: String) {
        val validation = Validators.validateMinLength(name, 2, "Employer name")
        _state.value = _state.value.copy(
            employerName = name,
            employerNameValidation = validation
        )
    }

    private fun updateJobTitle(title: String) {
        val validation = Validators.validateMinLength(title, 2, "Job title")
        _state.value = _state.value.copy(
            jobTitle = title,
            jobTitleValidation = validation
        )
    }

    private fun updateYearsEmployed(years: Float) {
        val validation = Validators.validateRange(years, 0f, 20f, "years employed")
        _state.value = _state.value.copy(
            yearsEmployed = years,
            yearsEmployedValidation = validation
        )
    }

    fun validateAll(): Boolean {
        val employerValidation = Validators.validateMinLength(
            _state.value.employerName, 2, "Employer name"
        )
        val titleValidation = Validators.validateMinLength(
            _state.value.jobTitle, 2, "Job title"
        )
        val yearsValidation = Validators.validateRange(
            _state.value.yearsEmployed, 0f, 20f, "years employed"
        )

        _state.value = _state.value.copy(
            employerNameValidation = employerValidation,
            jobTitleValidation = titleValidation,
            yearsEmployedValidation = yearsValidation
        )

        return employerValidation.isValid &&
               titleValidation.isValid &&
               yearsValidation.isValid
    }
}
```

**Key points:**
- Each field update validates immediately
- `validateAll()` called when attempting to proceed to next step
- Reuses `Validators` from core module

---

## Step 4: Create UI Screen

Create `features/employmentInfo/EmploymentInfoScreen.kt`:

```kotlin
package com.example.androidpractice.features.employmentInfo

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlin.math.roundToInt

@Composable
fun EmploymentInfoScreen(
    viewModel: EmploymentInfoViewModel,
    enabled: Boolean = true
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Employer Name
        Column {
            OutlinedTextField(
                value = state.employerName,
                onValueChange = {
                    viewModel.handleIntent(EmploymentInfoIntent.UpdateEmployerName(it))
                },
                label = { Text("Employer Name") },
                isError = !state.employerNameValidation.isValid,
                modifier = Modifier.fillMaxWidth(),
                enabled = enabled
            )
            if (!state.employerNameValidation.isValid) {
                Text(
                    text = state.employerNameValidation.errorMessage ?: "",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }

        // Job Title
        Column {
            OutlinedTextField(
                value = state.jobTitle,
                onValueChange = {
                    viewModel.handleIntent(EmploymentInfoIntent.UpdateJobTitle(it))
                },
                label = { Text("Job Title") },
                isError = !state.jobTitleValidation.isValid,
                modifier = Modifier.fillMaxWidth(),
                enabled = enabled
            )
            if (!state.jobTitleValidation.isValid) {
                Text(
                    text = state.jobTitleValidation.errorMessage ?: "",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }

        // Years Employed Slider
        Column {
            Text(
                text = "Years at Current Employer",
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = "${state.yearsEmployed.roundToInt()} years",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Slider(
                value = state.yearsEmployed,
                onValueChange = {
                    viewModel.handleIntent(EmploymentInfoIntent.UpdateYearsEmployed(it))
                },
                valueRange = 0f..20f,
                steps = 19,
                enabled = enabled,
                modifier = Modifier.fillMaxWidth()
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "0 years",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "20+ years",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (!state.yearsEmployedValidation.isValid) {
                Text(
                    text = state.yearsEmployedValidation.errorMessage ?: "",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}
```

**Key points:**
- Collects state from ViewModel using `collectAsStateWithLifecycle()`
- Shows error messages conditionally
- `enabled` parameter passed from coordinator during submission

---

## Step 5: Update FormStep Enum

Edit `features/form/FormStep.kt` and add your new step:

```kotlin
package com.example.androidpractice.features.form

enum class FormStep(val displayName: String) {
    LOAN_DETAILS("Loan Details"),
    PERSONAL_INFO("Personal Information"),
    EMPLOYMENT_INFO("Employment Info")  // ← ADD THIS
}
```

**Important:** Order matters! The enum ordinal determines step sequence.

---

## Step 6: Wire into FormCoordinator

Edit `features/form/FormCoordinator.kt`:

### 6a. Add ViewModel Parameter

```kotlin
class FormCoordinator(
    val loanDetailsViewModel: LoanDetailsViewModel,
    val personalInfoViewModel: PersonalInfoViewModel,
    val employmentInfoViewModel: EmploymentInfoViewModel  // ← ADD THIS
) : ViewModel() {
```

### 6b. Update `nextStep()` Validation

```kotlin
private fun nextStep() {
    val currentValid = when (_state.value.currentStep) {
        FormStep.LOAN_DETAILS -> loanDetailsViewModel.validateAll()
        FormStep.PERSONAL_INFO -> personalInfoViewModel.validateAll()
        FormStep.EMPLOYMENT_INFO -> employmentInfoViewModel.validateAll()  // ← ADD THIS
    }

    if (currentValid && !_state.value.isLastStep) {
        val nextStep = FormStep.fromIndex(_state.value.currentStep.ordinal + 1)
        nextStep?.let {
            _state.value = _state.value.copy(currentStep = it)
        }
    }
}
```

### 6c. Update `submitForm()` Validation

```kotlin
private fun submitForm() {
    viewModelScope.launch {
        val allValid = loanDetailsViewModel.validateAll() &&
                      personalInfoViewModel.validateAll() &&
                      employmentInfoViewModel.validateAll()  // ← ADD THIS

        if (!allValid) {
            _state.value = _state.value.copy(
                result = LoanResponse(
                    success = false,
                    message = "Please fix all validation errors"
                )
            )
            return@launch
        }

        // ... rest of submit logic
    }
}
```

### 6d. Update `canProceed()` Function

```kotlin
fun canProceed(): Boolean {
    return when (_state.value.currentStep) {
        FormStep.LOAN_DETAILS -> loanDetailsViewModel.state.value.isValid
        FormStep.PERSONAL_INFO -> personalInfoViewModel.state.value.isValid
        FormStep.EMPLOYMENT_INFO -> employmentInfoViewModel.state.value.isValid  // ← ADD THIS
    }
}
```

---

## Step 7: Add Screen to FormScreen UI

Edit `features/form/FormScreen.kt` and add the screen case:

```kotlin
// Step content
when (formState.currentStep) {
    FormStep.LOAN_DETAILS -> {
        LoanDetailsScreen(
            viewModel = coordinator.loanDetailsViewModel,
            enabled = !formState.isSubmitting
        )
    }
    FormStep.PERSONAL_INFO -> {
        PersonalInfoScreen(
            viewModel = coordinator.personalInfoViewModel,
            enabled = !formState.isSubmitting
        )
    }
    FormStep.EMPLOYMENT_INFO -> {  // ← ADD THIS
        EmploymentInfoScreen(
            viewModel = coordinator.employmentInfoViewModel,
            enabled = !formState.isSubmitting
        )
    }
}
```

---

## Step 8: Wire Up in MainActivity

Edit `MainActivity.kt` to create and pass the ViewModel:

```kotlin
// Create feature ViewModels
val loanDetailsViewModel: LoanDetailsViewModel = viewModel()
val personalInfoViewModel: PersonalInfoViewModel = viewModel()
val employmentInfoViewModel: EmploymentInfoViewModel = viewModel()  // ← ADD THIS

// Create coordinator
val coordinator = remember {
    FormCoordinator(
        loanDetailsViewModel = loanDetailsViewModel,
        personalInfoViewModel = personalInfoViewModel,
        employmentInfoViewModel = employmentInfoViewModel  // ← ADD THIS
    )
}
```

---

## Step 9: Build and Test

```bash
./gradlew assembleDebug
```

**Verify:**
1. ✅ New step appears in step indicator
2. ✅ Navigation works (Previous/Next buttons)
3. ✅ Validation blocks progression when fields invalid
4. ✅ Error messages appear for invalid fields
5. ✅ Submit button appears only on last step
6. ✅ Form submits successfully with all data

---

## Quick Checklist

When adding a new page, ensure you:

- [ ] Created feature directory: `features/yourFeature/`
- [ ] Created `YourFeatureState.kt` with state and intents
- [ ] Created `YourFeatureViewModel.kt` with validation logic
- [ ] Created `YourFeatureScreen.kt` with UI components
- [ ] Added step to `FormStep` enum
- [ ] Added ViewModel parameter to `FormCoordinator`
- [ ] Added validation case in `nextStep()`
- [ ] Added validation case in `submitForm()`
- [ ] Added validation case in `canProceed()`
- [ ] Added UI case in `FormScreen.kt`
- [ ] Instantiated ViewModel in `MainActivity.kt`
- [ ] Passed ViewModel to coordinator in `MainActivity.kt`
- [ ] Tested all navigation and validation

---

## Tips & Best Practices

### Validation
- Use existing `Validators` from `core/validation/` when possible
- Add custom validators if needed (e.g., SSN format, phone number)
- Validate on every field change for immediate feedback
- Validate all fields in `validateAll()` before step transition

### State Management
- Keep feature state flat and simple
- Use computed properties for derived state (`isValid`)
- Avoid nested data structures when possible

### UI Components
- Reuse Material 3 components
- Show errors below fields with proper spacing
- Disable fields during submission (`enabled` parameter)
- Use descriptive labels and helper text

### Testing
- Test ViewModel logic independently
- Mock validations for easier testing
- Test coordinator flow with fake ViewModels

---

## Example: Adding a Dropdown Field

If you need a dropdown (like loan type), use `ExposedDropdownMenuBox`:

```kotlin
// In State
val employmentStatus: EmploymentStatus? = null

enum class EmploymentStatus(val display: String) {
    FULL_TIME("Full Time"),
    PART_TIME("Part Time"),
    CONTRACT("Contract"),
    SELF_EMPLOYED("Self-Employed")
}

// In Screen
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StatusDropdown(
    selected: EmploymentStatus?,
    onSelected: (EmploymentStatus) -> Unit,
    enabled: Boolean
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded && enabled }
    ) {
        OutlinedTextField(
            value = selected?.display ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text("Employment Status") },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            modifier = Modifier.fillMaxWidth().menuAnchor(),
            enabled = enabled
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            EmploymentStatus.entries.forEach { status ->
                DropdownMenuItem(
                    text = { Text(status.display) },
                    onClick = {
                        onSelected(status)
                        expanded = false
                    }
                )
            }
        }
    }
}
```

---

## Common Pitfalls

❌ **Forgetting to add validation case in coordinator**
- Symptom: Can't proceed to next step even when fields are valid
- Fix: Add case in `canProceed()` and validation logic

❌ **Wrong enum order in FormStep**
- Symptom: Steps appear out of order
- Fix: Ensure enum order matches desired flow

❌ **Not disabling fields during submission**
- Symptom: Users can edit fields while form is submitting
- Fix: Pass `enabled = !formState.isSubmitting` to screen

❌ **Forgetting to add ViewModel to MainActivity**
- Symptom: Compilation error - unresolved reference
- Fix: Create ViewModel and pass to coordinator

---

## Need More Complex Validation?

Add custom validators in `core/validation/Validators.kt`:

```kotlin
object Validators {
    // Existing validators...

    fun validateSSN(ssn: String): FieldValidation {
        val ssnRegex = "^\\d{3}-\\d{2}-\\d{4}$".toRegex()
        return when {
            ssn.isBlank() -> FieldValidation.Invalid("SSN is required")
            !ssnRegex.matches(ssn) -> FieldValidation.Invalid("SSN must be in format XXX-XX-XXXX")
            else -> FieldValidation.Valid
        }
    }

    fun validatePhoneNumber(phone: String): FieldValidation {
        val phoneRegex = "^\\d{3}-\\d{3}-\\d{4}$".toRegex()
        return when {
            phone.isBlank() -> FieldValidation.Invalid("Phone is required")
            !phoneRegex.matches(phone) -> FieldValidation.Invalid("Phone must be in format XXX-XXX-XXXX")
            else -> FieldValidation.Valid
        }
    }
}
```

---

## Summary

Adding a new page requires:
1. **Create** feature module (3 files)
2. **Update** FormStep enum
3. **Wire** into FormCoordinator (3 places)
4. **Add** UI case in FormScreen
5. **Instantiate** in MainActivity

The architecture makes it easy to add new steps without modifying existing features! 🚀
