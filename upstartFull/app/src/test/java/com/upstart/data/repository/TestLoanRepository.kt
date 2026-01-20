package com.upstart.data.repository

import com.upstart.core.models.LoanResponse
import com.upstart.core.models.LoanType

/**
 * Lightweight in-memory repository used by unit tests so they can assert on
 * the parameters passed to backend calls and control the returned results.
 */
class TestLoanRepository : LoanRepository {
    var submitLoanDetailsResult: Result<String> = Result.success("APP-12345")
    var submitPersonalInfoResult: Result<String> = Result.success("Saved")
    var getApplicationStatusResult: Result<LoanResponse> =
        Result.success(LoanResponse(success = true, message = "ok", applicationId = "APP-12345"))

    var submittedLoanDetails: Pair<Float, LoanType>? = null
        private set
    var submittedPersonalInfo: Triple<String, String, String>? = null
        private set
    var requestedApplicationId: String? = null
        private set

    override suspend fun submitLoanDetails(amount: Float, loanType: LoanType): String {
        submittedLoanDetails = amount to loanType
        return submitLoanDetailsResult.getOrThrow()
    }

    override suspend fun submitPersonalInfo(firstName: String, lastName: String, email: String): String {
        submittedPersonalInfo = Triple(firstName, lastName, email)
        return submitPersonalInfoResult.getOrThrow()
    }

    override suspend fun getApplicationStatus(applicationId: String): LoanResponse {
        requestedApplicationId = applicationId
        return getApplicationStatusResult.getOrThrow()
    }
}
