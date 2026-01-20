package com.upstart.core.models

// API request/response models
data class LoanRequest(
    val amount: Int,
    val loanType: String,
    val firstName: String,
    val lastName: String,
    val email: String
)

data class LoanResponse(
    val success: Boolean,
    val message: String,
    val applicationId: String? = null
)
