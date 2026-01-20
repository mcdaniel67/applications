package com.upstart.minimal.data.remote

import com.upstart.minimal.data.remote.dto.LoanSubmissionRequestDto
import com.upstart.minimal.data.remote.dto.LoanSubmissionResponseDto
import kotlinx.coroutines.delay
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

interface LoanApi {
    suspend fun submitLoanApplication(requestJson: String): String
}

class MockLoanApi(
    private val json: Json = Json { ignoreUnknownKeys = true }
) : LoanApi {

    override suspend fun submitLoanApplication(requestJson: String): String {
        delay(800L)
        val request = json.decodeFromString<LoanSubmissionRequestDto>(requestJson)
        val approved = request.amount <= 15000
        val response = LoanSubmissionResponseDto(
            confirmationId = "UP-${System.currentTimeMillis()}",
            approved = approved,
            message = if (approved) {
                "Congrats ${request.borrowerName}, your ${request.amount} loan is provisionally approved."
            } else {
                "Thanks ${request.borrowerName}, we need a manual review for your request."
            }
        )
        return json.encodeToString(response)
    }
}
