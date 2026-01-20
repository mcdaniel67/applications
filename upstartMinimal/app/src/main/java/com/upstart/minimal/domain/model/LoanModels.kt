package com.upstart.minimal.domain.model

data class LoanPurpose(
    val id: String,
    val label: String
)

data class LoanSubmissionRequest(
    val purposeId: String,
    val amount: Int,
    val borrowerName: String
)

data class LoanSubmissionResult(
    val referenceId: String,
    val approved: Boolean,
    val message: String
)
