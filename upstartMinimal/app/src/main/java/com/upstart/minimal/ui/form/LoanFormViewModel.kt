package com.upstart.minimal.ui.form

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.upstart.minimal.data.LoanRepository
import com.upstart.minimal.data.remote.MockLoanApi
import com.upstart.minimal.domain.model.LoanPurpose
import com.upstart.minimal.domain.model.LoanSubmissionRequest
import com.upstart.minimal.domain.model.LoanSubmissionResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlin.math.roundToInt

private object LoanFormDefaults {
    val purposes = listOf(
        LoanPurpose("credit_card", "Credit Card Refinance"),
        LoanPurpose("home_improvement", "Home Improvement"),
        LoanPurpose("auto_purchase", "Auto Purchase")
    )
    const val minAmount = 1000
    const val maxAmount = 20000
    const val defaultAmount = 7500
    const val amountStep = 500
}

data class LoanFormState(
    val purposes: List<LoanPurpose> = LoanFormDefaults.purposes,
    val selectedPurposeId: String? = LoanFormDefaults.purposes.firstOrNull()?.id,
    val minAmount: Int = LoanFormDefaults.minAmount,
    val maxAmount: Int = LoanFormDefaults.maxAmount,
    val amountStep: Int = LoanFormDefaults.amountStep,
    val loanAmount: Float = LoanFormDefaults.defaultAmount.toFloat(),
    val borrowerName: String = "",
    val borrowerNameError: String? = null,
    val isSubmitting: Boolean = false,
    val submissionResult: LoanSubmissionResult? = null,
    val errorMessage: String? = null
)

sealed interface LoanFormEvent {
    data class PurposeSelected(val purposeId: String) : LoanFormEvent
    data class LoanAmountChanged(val amount: Float) : LoanFormEvent
    data class BorrowerNameChanged(val value: String) : LoanFormEvent
    data object Submit : LoanFormEvent
    data object DismissResult : LoanFormEvent
}

class LoanFormViewModel(
    private val repository: LoanRepository
) : ViewModel() {

    private val _state = MutableStateFlow(LoanFormState())
    val state: StateFlow<LoanFormState> = _state.asStateFlow()

    fun onEvent(event: LoanFormEvent) {
        when (event) {
            is LoanFormEvent.PurposeSelected -> {
                _state.update { it.copy(selectedPurposeId = event.purposeId) }
            }

            is LoanFormEvent.LoanAmountChanged -> {
                val currentState = _state.value
                val snapped = snapToStep(
                    value = event.amount,
                    min = currentState.minAmount,
                    max = currentState.maxAmount
                )
                _state.update { it.copy(loanAmount = snapped) }
            }

            is LoanFormEvent.BorrowerNameChanged -> {
                _state.update { it.copy(borrowerName = event.value, borrowerNameError = null) }
            }

            LoanFormEvent.Submit -> submitForm()
            LoanFormEvent.DismissResult -> _state.update { it.copy(submissionResult = null) }
        }
    }

    private fun submitForm() {
        val currentState = _state.value

        val purposeId = currentState.selectedPurposeId
        if (purposeId.isNullOrBlank()) {
            return
        }

        if (currentState.borrowerName.isBlank()) {
            _state.update { it.copy(borrowerNameError = "Borrower name is required") }
            return
        }

        if (currentState.isSubmitting) return

        val request = LoanSubmissionRequest(
            purposeId = purposeId,
            amount = currentState.loanAmount.roundToInt()
                .coerceIn(currentState.minAmount, currentState.maxAmount),
            borrowerName = currentState.borrowerName.trim()
        )

        viewModelScope.launch {
            _state.update { it.copy(isSubmitting = true, errorMessage = null) }
            repository.submitLoanApplication(request)
                .onSuccess { result ->
                    _state.update {
                        it.copy(
                            isSubmitting = false,
                            submissionResult = result
                        )
                    }
                }
                .onFailure { throwable ->
                    val message = throwable.message ?: "Submission failed"
                    _state.update {
                        it.copy(
                            isSubmitting = false,
                            errorMessage = message
                        )
                    }
                }
        }
    }

    private fun snapToStep(value: Float, min: Int, max: Int): Float {
        val clamped = value.coerceIn(min.toFloat(), max.toFloat())
        val relative = (clamped - min) / LoanFormDefaults.amountStep
        val snappedSteps = relative.roundToInt()
        val snapped = min + snappedSteps * LoanFormDefaults.amountStep
        return snapped.coerceIn(min, max).toFloat()
    }
}

class LoanFormViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LoanFormViewModel::class.java)) {
            val json = Json { ignoreUnknownKeys = true }
            val api = MockLoanApi(json)
            val repository = LoanRepository(api, json)
            @Suppress("UNCHECKED_CAST")
            return LoanFormViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
