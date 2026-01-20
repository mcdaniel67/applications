# Architecture Overview for GenAI Agents

This document provides technical details about the Android loan application architecture for AI agents working on this codebase.

## Project Structure

```
app/src/main/java/com/upstart/
├── core/
│   ├── form/
│   │   ├── FormStepViewModel.kt        # Interface for step ViewModels
│   │   └── SubmissionState.kt          # Sealed class for submission states
│   ├── models/
│   │   ├── LoanType.kt                 # Enum: AUTO, MORTGAGE, PERSONAL, STUDENT, BUSINESS, HOME_IMPROVEMENT
│   │   └── LoanResponse.kt             # Data class for API responses
│   ├── config/
│   │   └── FormConfig.kt               # Constants (MIN_LOAN_AMOUNT, MAX_LOAN_AMOUNT, MIN_NAME_LENGTH)
│   ├── validation/
│   │   ├── Validators.kt               # Validation functions (email, range, minLength, required)
│   │   └── ValidationResult.kt         # Data class (isValid, errorMessage)
│   ├── theme/
│   │   ├── Color.kt                    # Material 3 ColorScheme (LightColorScheme, DarkColorScheme)
│   │   ├── Theme.kt                    # UpstartTheme composable
│   │   ├── Type.kt                     # Custom typography (UpstartTypography)
│   │   └── TextFieldDefaults.kt        # Themed text field colors
│   └── MainActivity.kt                 # Entry point, sets up theme and FormCoordinator
│
├── features/
│   ├── loanDetails/
│   │   ├── LoanDetailsViewModel.kt     # Manages amount & loan type, submits to API, stores applicationId
│   │   ├── LoanDetailsScreen.kt        # UI: Slider + Dropdown + Compose Previews
│   │   ├── LoanDetailsState.kt         # Data class with amount, loanType, validations, applicationId
│   │   └── LoanDetailsIntent.kt        # Sealed class: UpdateAmount, UpdateLoanType
│   │
│   ├── personalInfo/
│   │   ├── PersonalInfoViewModel.kt    # Manages firstName, lastName, email, submits to API
│   │   ├── PersonalInfoScreen.kt       # UI: Text fields + Compose Previews
│   │   ├── PersonalInfoState.kt        # Data class with fields and validations
│   │   └── PersonalInfoIntent.kt       # Sealed class: UpdateFirstName, UpdateLastName, UpdateEmail
│   │
│   ├── result/
│   │   ├── ResultViewModel.kt          # Fetches application status via GET request
│   │   ├── ResultStepScreen.kt         # UI: Success/Error display + Compose Previews
│   │   └── ResultState.kt              # Sealed class: Loading, Success(LoanResponse), Error(String)
│   │
│   └── form/
│       ├── FormCoordinator.kt          # Orchestrates multi-step flow, validates steps, handles navigation
│       ├── FormScreen.kt               # Container with step indicator + navigation buttons
│       ├── FormStep.kt                 # Enum: LOAN_DETAILS, PERSONAL_INFO, RESULT
│       ├── FormState.kt                # Data class: currentStep, isLoadingStep, result
│       └── FormIntent.kt               # Sealed class: NextStep, PreviousStep, Submit, Reset
│
└── data/
    └── repository/
        ├── LoanRepository.kt           # Interface with suspend functions
        └── FakeLoanRepository.kt       # Mock implementation with delays and random failures
```

## Architecture Pattern: Feature-Sliced + MVI

### Core Principles

1. **Feature Isolation**: Each feature (loanDetails, personalInfo, result) is self-contained
2. **Unidirectional Data Flow**: Intent → ViewModel → State → UI
3. **Independent Submission**: Each step submits its own data to the backend
4. **Coordinator Pattern**: FormCoordinator orchestrates flow without business logic
5. **Material 3 Integration**: Native Material Design 3 theming (no custom color wrapper)

### Data Flow

```
User Action (Intent)
    ↓
ViewModel.handleIntent()
    ↓
Update State (validation)
    ↓
UI Re-renders (StateFlow.collectAsState())
    ↓
[On Submit] → Repository → Update State → Coordinator decides next step
```

## Key Components

### 1. FormCoordinator (form/FormCoordinator.kt)

**Responsibilities:**
- Manages currentStep (LOAN_DETAILS → PERSONAL_INFO → RESULT)
- Validates current step before allowing NextStep
- Calls ViewModel.submit() for each step
- Navigates based on SubmissionState (Success/Error)
- Provides reset() to return to initial state

**State Management:**
```kotlin
data class FormState(
    val currentStep: FormStep = FormStep.LOAN_DETAILS,
    val isLoadingStep: Boolean = false,
    val result: LoanResponse? = null
)
```

**Key Methods:**
- `handleIntent(FormIntent)`: Routes NextStep, PreviousStep, Submit, Reset
- `nextStep()`: Validates current, submits data, navigates on success
- `canProceed()`: Returns whether current step is valid

### 2. Feature ViewModels

#### LoanDetailsViewModel (features/loanDetails/)

**Responsibilities:**
- Manages loan amount (1000f-50000f slider)
- Manages loan type selection (enum dropdown)
- Validates amount range and required loan type
- Submits to `repository.submitLoanDetails()` → receives applicationId
- Stores applicationId in state for Result screen

**State:**
```kotlin
data class LoanDetailsState(
    val amount: Float = 5000f,
    val loanType: LoanType? = null,
    val amountValidation: ValidationResult = ValidationResult.valid(),
    val loanTypeValidation: ValidationResult = ValidationResult.valid(),
    val applicationId: String? = null
) {
    val isValid: Boolean get() = amountValidation.isValid && loanTypeValidation.isValid
}
```

**Submission Flow:**
1. User clicks "Next" → Coordinator calls validateAll()
2. If valid → Coordinator calls submit()
3. submit() calls repository.submitLoanDetails(amount, loanType)
4. Repository returns applicationId (UUID)
5. ViewModel updates state.applicationId
6. SubmissionState.Success emitted
7. Coordinator advances to PERSONAL_INFO

#### PersonalInfoViewModel (features/personalInfo/)

**Responsibilities:**
- Manages firstName, lastName, email fields
- Validates min length (2 chars) and email format
- Submits to `repository.submitPersonalInfo()`

**Submission Flow:**
1. User clicks "Submit Application" → Coordinator validates all steps
2. If all valid → Coordinator calls submit()
3. submit() calls repository.submitPersonalInfo(firstName, lastName, email)
4. Repository saves data and returns success message
5. SubmissionState.Success emitted
6. Coordinator navigates to RESULT

#### ResultViewModel (features/result/)

**Responsibilities:**
- Fetches application status via GET request
- Displays success/failure message
- Shows applicationId received from LoanDetailsViewModel

**State Machine:**
```kotlin
sealed class ResultState {
    data object Loading : ResultState()
    data class Success(val result: LoanResponse) : ResultState()
    data class Error(val message: String) : ResultState()
}
```

**Fetching Flow:**
1. ResultStepScreen receives applicationId from FormScreen
2. LaunchedEffect triggers fetchApplicationStatus(applicationId)
3. ViewModel calls repository.getApplicationStatus(applicationId)
4. Updates state to Success(LoanResponse) or Error(message)

### 3. Repository Pattern

**Interface:** `LoanRepository`
```kotlin
interface LoanRepository {
    suspend fun submitLoanDetails(amount: Float, loanType: LoanType): String  // Returns applicationId
    suspend fun submitPersonalInfo(firstName: String, lastName: String, email: String): String
    suspend fun getApplicationStatus(applicationId: String): LoanResponse
}
```

**Implementation:** `FakeLoanRepository`
- Simulates 1-2 second delays
- 10% random failure rate for testing error states
- Returns mock success responses with timestamps

### 4. Validation System (core/validation/)

**Validators.kt** provides:
- `validateRequired(value, fieldName)`: Checks non-blank
- `validateMinLength(value, minLength, fieldName)`: Length validation
- `validateEmail(value)`: Regex pattern matching
- `validateRange(value, min, max, fieldName)`: Numeric range

**ValidationResult:**
```kotlin
data class ValidationResult(
    val isValid: Boolean,
    val errorMessage: String? = null
) {
    companion object {
        fun valid() = ValidationResult(true, null)
        fun error(message: String) = ValidationResult(false, message)
    }
}
```

### 5. Material 3 Theming (core/theme/)

**Color.kt:**
- Defines `LightColorScheme` and `DarkColorScheme` using Material 3's `lightColorScheme()`/`darkColorScheme()`
- Maps brand colors (UpstartColors) to Material 3 roles (primary, secondary, tertiary, etc.)
- Uses Material 3 ColorScheme directly (not a custom wrapper)

**Theme.kt:**
- `UpstartTheme` composable wraps MaterialTheme
- Provides custom typography via CompositionLocalProvider
- Colors accessed via `MaterialTheme.colorScheme.primary` (standard Material 3 pattern)
- Typography accessed via `UpstartTheme.typography.headlineSmall`

**TextFieldDefaults.kt:**
- Provides themed OutlinedTextField colors
- Uses `MaterialTheme.colorScheme` for dynamic theming

### 6. Compose Previews

Each screen has multiple @Preview functions for:
- Light Mode
- Dark Mode (when applicable)
- Valid state
- Invalid state
- Loading state
- Error state
- Disabled state

**Naming Convention:** `[Screen]Preview_[Mode]_[State]`
Example: `LoanDetailsPreview_Light_Valid`, `ResultPreview_Dark_Error`

## State Management Details

### StateFlow Pattern

All ViewModels use:
```kotlin
private val _state = MutableStateFlow(InitialState())
val state: StateFlow<State> = _state.asStateFlow()
```

UI collects with:
```kotlin
val state by viewModel.state.collectAsStateWithLifecycle()
```

### SubmissionState Pattern

```kotlin
sealed class SubmissionState {
    data object Idle : SubmissionState()
    data object Loading : SubmissionState()
    data class Success(val message: String?) : SubmissionState()
    data class Error(val message: String) : SubmissionState()
}
```

ViewModels implement `FormStepViewModel` interface:
```kotlin
interface FormStepViewModel {
    val submissionState: StateFlow<SubmissionState>
    val isValid: Boolean
    suspend fun submit()
    fun validateAll(): Boolean
}
```

## Testing Strategy

### Unit Tests (app/src/test/)

**Test Tools:**
- `kotlinx-coroutines-test`: StandardTestDispatcher, runTest
- `Turbine`: Flow testing library for StateFlow assertions
- `JUnit 4`: Test framework

**Test Pattern Example:**
```kotlin
@Test
fun `UpdateAmount intent updates amount and validates`() = runTest {
    viewModel.state.test {
        skipItems(1) // Skip initial state

        viewModel.handleIntent(LoanDetailsIntent.UpdateAmount(10000f))
        testDispatcher.scheduler.advanceUntilIdle()

        val state = awaitItem()
        assertEquals(10000f, state.amount)
        assertTrue(state.amountValidation.isValid)
    }
}
```

## Navigation Flow

```
MainActivity
    └─ FormScreen(coordinator)
        ├─ [Step Indicator]
        ├─ when(currentStep) {
        │   LOAN_DETAILS → LoanDetailsScreen(loanDetailsViewModel)
        │   PERSONAL_INFO → PersonalInfoScreen(personalInfoViewModel)
        │   RESULT → ResultStepScreen(applicationId, onStartOver)
        │  }
        └─ [Navigation Buttons: Previous, Next/Submit]
```

**Key Points:**
- No Jetpack Navigation library used
- State-based navigation via FormState.currentStep
- ViewModels scoped to MainActivity (survive configuration changes)
- FormCoordinator created with factory pattern to inject child ViewModels

## Build Configuration

**Gradle:**
- Kotlin 1.9.20
- Compose BOM 2024.02.00
- Material 3
- Coroutines 1.7.3
- Turbine 1.0.0 (testing)

**Commands:**
```bash
./gradlew assembleDebug    # Build debug APK
./gradlew test             # Run unit tests
./gradlew clean            # Clean build artifacts
```

## Key Design Patterns

1. **MVI (Model-View-Intent)**: Unidirectional data flow
2. **Feature-Sliced Architecture**: Features are independent, self-contained modules
3. **Repository Pattern**: Abstraction over data sources
4. **Coordinator Pattern**: Orchestrates flow without business logic
5. **Factory Pattern**: ViewModel creation with dependencies
6. **State Machine**: Explicit state modeling (Loading, Success, Error)
7. **Validation Strategy**: Centralized, reusable validators

## Extensibility Points

### Adding a New Form Step

1. Create feature directory: `features/newStep/`
2. Create ViewModel implementing `FormStepViewModel`
3. Create Screen composable
4. Add enum value to `FormStep`
5. Add case to `FormScreen.kt` when statement
6. Update `FormCoordinator` navigation logic
7. Add unit tests

### Adding Server-Side Validation

1. Add async validation method to Validators
2. Call from ViewModel after user input
3. Update ValidationResult with loading state
4. Display error messages in UI

### Adding State Persistence

1. Inject SavedStateHandle to ViewModels
2. Use `savedStateHandle.getStateFlow()` for state
3. State automatically persists across process death

### Adding Real API

1. Add Retrofit dependency
2. Create ApiService interface
3. Implement RealLoanRepository
4. Update error handling for network failures

## Common Patterns

### Intent Handler Pattern
```kotlin
fun handleIntent(intent: Intent) {
    when (intent) {
        is Intent.Action1 -> handleAction1(intent.data)
        is Intent.Action2 -> handleAction2()
    }
}
```

### Validation on Update
```kotlin
private fun updateField(value: String) {
    val validation = Validators.validate(value)
    _state.value = _state.value.copy(
        field = value,
        fieldValidation = validation
    )
}
```

### Submit Pattern
```kotlin
override suspend fun submit() {
    if (!validateAll()) {
        _submissionState.value = SubmissionState.Error("Fix validation errors")
        return
    }

    _submissionState.value = SubmissionState.Loading

    try {
        val result = repository.submitData(...)
        _submissionState.value = SubmissionState.Success(result)
    } catch (e: Exception) {
        _submissionState.value = SubmissionState.Error(e.message ?: "Unknown error")
    }
}
```

## Files Reference Quick Lookup

| Component | File Path | Key Responsibility |
|-----------|-----------|-------------------|
| Main Entry | core/MainActivity.kt | Theme setup, ViewModel initialization |
| Form Container | features/form/FormScreen.kt | Step indicator, navigation UI |
| Step 1 UI | features/loanDetails/LoanDetailsScreen.kt | Slider + Dropdown |
| Step 2 UI | features/personalInfo/PersonalInfoScreen.kt | Text fields |
| Step 3 UI | features/result/ResultStepScreen.kt | Success/Error display |
| Orchestration | features/form/FormCoordinator.kt | Step navigation, validation |
| Validation | core/validation/Validators.kt | Reusable validation functions |
| Mock Data | data/repository/FakeLoanRepository.kt | Simulated API |
| Theming | core/theme/Color.kt | Material 3 color schemes |
| Typography | core/theme/Type.kt | Custom font styles |

## Testing Quick Reference

| Test File | Coverage |
|-----------|----------|
| ValidatorsTest.kt | 15 tests - all validation functions |
| LoanDetailsViewModelTest.kt | 17 tests - state, intents, submit, reset |
| PersonalInfoViewModelTest.kt | 18 tests - field updates, validation, submit |
| FormCoordinatorTest.kt | 17 tests - navigation, validation, full flow |
| ResultViewModelTest.kt | 8 tests - fetch status, states, reset |

**Total:** 75 unit tests covering core business logic

## Configuration Values

Located in `core/config/FormConfig.kt`:
```kotlin
object FormConfig {
    const val MIN_LOAN_AMOUNT = 1000f
    const val MAX_LOAN_AMOUNT = 50000f
    const val MIN_NAME_LENGTH = 2
}
```

## Error Handling

- **Validation Errors**: Displayed inline under fields
- **Network Errors**: Shown in Result screen or as inline message
- **Random Failures**: FakeLoanRepository has 10% failure rate for testing

## Notes for AI Agents

1. **Do not** add DI framework unless explicitly requested
2. **Follow** existing patterns for new features
3. **Test** all ViewModel logic with Turbine
4. **Use** Material 3 ColorScheme directly (not custom wrappers)
5. **Validate** on every user input for real-time feedback
6. **Keep** features isolated and self-contained
7. **Use** StandardTestDispatcher for deterministic test execution
8. **Advance** dispatcher with `testDispatcher.scheduler.advanceUntilIdle()`
9. **Collect** flows with Turbine's `test {}` block
10. **Reset** state fully in reset() methods (both state and submissionState)
