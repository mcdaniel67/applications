package com.upstart.features.form

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.upstart.core.form.SubmissionState
import com.upstart.core.models.LoanResponse
import com.upstart.features.loanDetails.LoanDetailsViewModel
import com.upstart.features.personalInfo.PersonalInfoViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.IOException

// Overall form state
data class FormState(
    val currentStep: FormStep = FormStep.LOAN_DETAILS,
    val isLoadingStep: Boolean = false,
    val result: LoanResponse? = null
) {
    val canGoBack: Boolean get() = currentStep.ordinal > 0 && currentStep != FormStep.RESULT
    val isLastStep: Boolean get() = currentStep == FormStep.PERSONAL_INFO
}

// Form coordinator intents
sealed class FormIntent {
    data object NextStep : FormIntent()
    data object PreviousStep : FormIntent()
    data object Submit : FormIntent()
    data object Reset : FormIntent()
}

/**
 * Coordinator ViewModel that orchestrates the multi-step form flow.
 * Delegates field-specific logic to feature ViewModels.
 */
class FormCoordinator(
    val loanDetailsViewModel: LoanDetailsViewModel,
    val personalInfoViewModel: PersonalInfoViewModel
) : ViewModel() {

    private val _state = MutableStateFlow(FormState())
    val state: StateFlow<FormState> = _state.asStateFlow()

    fun handleIntent(intent: FormIntent) {
        when (intent) {
            is FormIntent.NextStep -> nextStep()
            is FormIntent.PreviousStep -> previousStep()
            is FormIntent.Submit -> submitForm()
            is FormIntent.Reset -> reset()
        }
    }

    private fun nextStep() {
        viewModelScope.launch {
            // Don't allow navigation from RESULT step or if already loading
            if (_state.value.currentStep == FormStep.RESULT || _state.value.isLoadingStep) return@launch

            // Validate current step
            val currentViewModel = when (_state.value.currentStep) {
                FormStep.LOAN_DETAILS -> loanDetailsViewModel
                FormStep.PERSONAL_INFO -> personalInfoViewModel
                FormStep.RESULT -> return@launch
            }

            if (!currentViewModel.validateAll()) {
                return@launch
            }

            // Set loading state
            _state.value = _state.value.copy(isLoadingStep = true, result = null)

            try {
                // Submit current step's data to backend
                currentViewModel.submit()

                // Check if submission was successful
                when (val submissionState = currentViewModel.submissionState.value) {
                    is SubmissionState.Error -> {
                        _state.value = _state.value.copy(
                            isLoadingStep = false,
                            result = LoanResponse(
                                success = false,
                                message = submissionState.message
                            )
                        )
                        return@launch
                    }
                    is SubmissionState.Success -> {
                        // Continue to next step
                        if (!_state.value.isLastStep) {
                            val nextStep = FormStep.fromIndex(_state.value.currentStep.ordinal + 1)
                            nextStep?.let {
                                _state.value = _state.value.copy(
                                    currentStep = it,
                                    isLoadingStep = false
                                )
                            }
                        } else {
                            _state.value = _state.value.copy(isLoadingStep = false)
                        }
                    }
                    else -> {
                        _state.value = _state.value.copy(isLoadingStep = false)
                    }
                }
            } catch (e: IOException) {
                _state.value = _state.value.copy(
                    isLoadingStep = false,
                    result = LoanResponse(
                        success = false,
                        message = e.message ?: "Failed to submit step data"
                    )
                )
            }
        }
    }

    private fun previousStep() {
        if (_state.value.canGoBack) {
            val prevStep = FormStep.fromIndex(_state.value.currentStep.ordinal - 1)
            prevStep?.let {
                _state.value = _state.value.copy(currentStep = it)
            }
        }
    }

    private fun submitForm() {
        viewModelScope.launch {
            // Validate all steps (except RESULT)
            val allValid = loanDetailsViewModel.validateAll() && personalInfoViewModel.validateAll()
            if (!allValid) {
                _state.value = _state.value.copy(
                    result = LoanResponse(
                        success = false,
                        message = "Please fix all validation errors"
                    )
                )
                return@launch
            }

            // Simply navigate to RESULT step
            // The ResultStepScreen will fetch the application status using the applicationId
            _state.value = _state.value.copy(
                currentStep = FormStep.RESULT
            )
        }
    }

    private fun reset() {
        _state.value = FormState()
        loanDetailsViewModel.reset()
        personalInfoViewModel.reset()
    }

    fun canProceed(): Boolean {
        return when (_state.value.currentStep) {
            FormStep.LOAN_DETAILS -> loanDetailsViewModel.isValid
            FormStep.PERSONAL_INFO -> personalInfoViewModel.isValid
            FormStep.RESULT -> false // Can't proceed from result step
        }
    }

    companion object {
        fun factory(
            loanDetailsViewModel: LoanDetailsViewModel,
            personalInfoViewModel: PersonalInfoViewModel
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(FormCoordinator::class.java)) {
                    return FormCoordinator(
                        loanDetailsViewModel = loanDetailsViewModel,
                        personalInfoViewModel = personalInfoViewModel
                    ) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
            }
        }
    }
}
