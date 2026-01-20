package com.upstart.features.personalInfo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.upstart.core.config.FormConfig
import com.upstart.core.form.FormStepViewModel
import com.upstart.core.form.SubmissionState
import com.upstart.core.validation.Validators
import com.upstart.data.repository.FakeLoanRepository
import com.upstart.data.repository.LoanRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PersonalInfoViewModel(
    private val repository: LoanRepository = FakeLoanRepository()
) : ViewModel(), FormStepViewModel {

    private val _state = MutableStateFlow(PersonalInfoState())
    val state: StateFlow<PersonalInfoState> = _state.asStateFlow()

    private val _submissionState = MutableStateFlow<SubmissionState>(SubmissionState.Idle)
    override val submissionState: StateFlow<SubmissionState> = _submissionState.asStateFlow()

    override val isValid: Boolean
        get() = _state.value.isValid

    fun handleIntent(intent: PersonalInfoIntent) {
        when (intent) {
            is PersonalInfoIntent.UpdateFirstName -> updateFirstName(intent.firstName)
            is PersonalInfoIntent.UpdateLastName -> updateLastName(intent.lastName)
            is PersonalInfoIntent.UpdateEmail -> updateEmail(intent.email)
        }
    }

    private fun updateFirstName(firstName: String) {
        val validation = Validators.validateMinLength(firstName, FormConfig.MIN_NAME_LENGTH, "First name")
        _state.value = _state.value.copy(
            firstName = firstName,
            firstNameValidation = validation
        )
    }

    private fun updateLastName(lastName: String) {
        val validation = Validators.validateMinLength(lastName, FormConfig.MIN_NAME_LENGTH, "Last name")
        _state.value = _state.value.copy(
            lastName = lastName,
            lastNameValidation = validation
        )
    }

    private fun updateEmail(email: String) {
        val validation = Validators.validateEmail(email)
        _state.value = _state.value.copy(
            email = email,
            emailValidation = validation
        )
    }

    override fun validateAll(): Boolean {
        val firstNameValidation = Validators.validateMinLength(_state.value.firstName, FormConfig.MIN_NAME_LENGTH, "First name")
        val lastNameValidation = Validators.validateMinLength(_state.value.lastName, FormConfig.MIN_NAME_LENGTH, "Last name")
        val emailValidation = Validators.validateEmail(_state.value.email)

        _state.value = _state.value.copy(
            firstNameValidation = firstNameValidation,
            lastNameValidation = lastNameValidation,
            emailValidation = emailValidation
        )

        return firstNameValidation.isValid && lastNameValidation.isValid && emailValidation.isValid
    }

    override suspend fun submit() {
        if (!validateAll()) {
            _submissionState.value = SubmissionState.Error("Please fix validation errors before submitting")
            return
        }

        _submissionState.value = SubmissionState.Loading

        try {
            val message = repository.submitPersonalInfo(
                firstName = _state.value.firstName,
                lastName = _state.value.lastName,
                email = _state.value.email
            )
            _submissionState.value = SubmissionState.Success(message)
        } catch (e: Exception) {
            _submissionState.value = SubmissionState.Error(e.message ?: "Failed to submit personal information")
        }
    }

    /**
     * Triggers submission in viewModelScope.
     * Useful for calling from UI without suspend context.
     */
    fun submitAsync() {
        viewModelScope.launch {
            submit()
        }
    }

    /**
     * Resets the ViewModel to its initial state.
     */
    fun reset() {
        _state.value = PersonalInfoState()
        _submissionState.value = SubmissionState.Idle
    }
}
