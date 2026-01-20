package com.upstart.features.loanDetails

import com.upstart.core.config.FormConfig
import com.upstart.core.models.LoanType
import com.upstart.core.validation.FieldValidation

// UI state for loan details step
data class LoanDetailsState(
    val amount: Float = 5000f,
    val loanType: LoanType? = null,
    val amountValidation: FieldValidation = FieldValidation.Valid,
    val loanTypeValidation: FieldValidation = FieldValidation.Valid,
    val applicationId: String? = null
) {
    val isValid: Boolean
        get() = amountValidation.isValid && loanTypeValidation.isValid
}

// User intents for loan details
sealed class LoanDetailsIntent {
    data class UpdateAmount(val amount: Float) : LoanDetailsIntent()
    data class UpdateLoanType(val loanType: LoanType) : LoanDetailsIntent()
}
