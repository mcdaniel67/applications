package com.upstart.minimal.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LoanSubmissionRequestDto(
    @SerialName("purpose_id") val purposeId: String,
    val amount: Int,
    @SerialName("borrower_name") val borrowerName: String
)

@Serializable
data class LoanSubmissionResponseDto(
    @SerialName("confirmation_id") val confirmationId: String,
    val approved: Boolean,
    val message: String
)
