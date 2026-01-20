package com.upstart.minimal.data

import com.upstart.minimal.data.remote.LoanApi
import com.upstart.minimal.data.remote.dto.LoanSubmissionRequestDto
import com.upstart.minimal.data.remote.dto.LoanSubmissionResponseDto
import com.upstart.minimal.domain.model.LoanSubmissionRequest
import com.upstart.minimal.domain.model.LoanSubmissionResult
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

class LoanRepository(
    private val api: LoanApi,
    private val json: Json = Json { ignoreUnknownKeys = true }
) {

    suspend fun submitLoanApplication(request: LoanSubmissionRequest): Result<LoanSubmissionResult> =
        runCatching {
            val dto = request.toDto()
            val requestJson = json.encodeToString(dto)
            val responseJson = api.submitLoanApplication(requestJson)
            val responseDto = json.decodeFromString<LoanSubmissionResponseDto>(responseJson)
            responseDto.toDomain()
        }
}

private fun LoanSubmissionResponseDto.toDomain(): LoanSubmissionResult {
    return LoanSubmissionResult(
        referenceId = confirmationId,
        approved = approved,
        message = message
    )
}

private fun LoanSubmissionRequest.toDto(): LoanSubmissionRequestDto {
    return LoanSubmissionRequestDto(
        purposeId = purposeId,
        amount = amount,
        borrowerName = borrowerName
    )
}
