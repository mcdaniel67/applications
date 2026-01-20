package com.upstart.features.loanDetails

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.upstart.core.config.FormConfig
import com.upstart.core.form.FormStepViewModel
import com.upstart.core.form.SubmissionState
import com.upstart.core.models.LoanType
import com.upstart.core.validation.FieldValidation
import com.upstart.core.validation.Validators
import com.upstart.data.repository.FakeLoanRepository
import com.upstart.data.repository.LoanRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.IOException

class LoanDetailsViewModel(
    private val repository: LoanRepository = FakeLoanRepository()
) : ViewModel(), FormStepViewModel {

    private val _state = MutableStateFlow(LoanDetailsState())
    val state: StateFlow<LoanDetailsState> = _state.asStateFlow()

    private val _submissionState = MutableStateFlow<SubmissionState>(SubmissionState.Idle)
    override val submissionState: StateFlow<SubmissionState> = _submissionState.asStateFlow()

    override val isValid: Boolean
        get() = _state.value.isValid

    fun handleIntent(intent: LoanDetailsIntent) {
        when (intent) {
            is LoanDetailsIntent.UpdateAmount -> updateAmount(intent.amount)
            is LoanDetailsIntent.UpdateLoanType -> updateLoanType(intent.loanType)
        }
    }

    private fun updateAmount(amount: Float) {
        val validation = Validators.validateRange(amount, FormConfig.MIN_LOAN_AMOUNT, FormConfig.MAX_LOAN_AMOUNT, "loan amount")
        _state.value = _state.value.copy(
            amount = amount,
            amountValidation = validation
        )
    }

    private fun updateLoanType(loanType: LoanType) {
        _state.value = _state.value.copy(
            loanType = loanType,
            loanTypeValidation = FieldValidation.Valid
        )
    }

    override fun validateAll(): Boolean {
        val amountValidation = Validators.validateRange(_state.value.amount, FormConfig.MIN_LOAN_AMOUNT, FormConfig.MAX_LOAN_AMOUNT, "loan amount")
        val loanTypeValidation = if (_state.value.loanType == null) {
            FieldValidation.Invalid("Please select a loan type")
        } else {
            FieldValidation.Valid
        }

        _state.value = _state.value.copy(
            amountValidation = amountValidation,
            loanTypeValidation = loanTypeValidation
        )

        return amountValidation.isValid && loanTypeValidation.isValid
    }

    override suspend fun submit() {
        if (!validateAll()) {
            _submissionState.value = SubmissionState.Error("Please fix validation errors before submitting")
            return
        }

        _submissionState.value = SubmissionState.Loading

        try {
            val loanType = _state.value.loanType ?: throw IllegalStateException("Loan type is null")
            val applicationId = repository.submitLoanDetails(_state.value.amount, loanType)

            // Store the applicationId in state
            _state.value = _state.value.copy(applicationId = applicationId)

            _submissionState.value = SubmissionState.Success("Loan details saved with ID: $applicationId")
        } catch (e: IOException) {
            _submissionState.value = SubmissionState.Error(e.message ?: "Failed to submit loan details")
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
        _state.value = LoanDetailsState()
        _submissionState.value = SubmissionState.Idle
    }
}
