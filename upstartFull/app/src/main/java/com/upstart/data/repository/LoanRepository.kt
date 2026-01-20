package com.upstart.data.repository

import com.upstart.core.models.LoanType

import com.upstart.core.models.LoanResponse

/**
 * Repository interface for loan-related backend operations.
 */
interface LoanRepository {
    /**
     * Submits loan details to the backend and creates a new application.
     *
     * @param amount The loan amount
     * @param loanType The type of loan
     * @return Application ID (UUID) for this loan application
     */
    suspend fun submitLoanDetails(amount: Float, loanType: LoanType): String

    /**
     * Submits personal information to the backend.
     *
     * @param firstName User's first name
     * @param lastName User's last name
     * @param email User's email address
     * @return Success message or throws exception on failure
     */
    suspend fun submitPersonalInfo(firstName: String, lastName: String, email: String): String

    /**
     * Retrieves the status of a loan application by its ID.
     * This is called on the Results page to get the final application status.
     *
     * @param applicationId The UUID of the loan application
     * @return LoanResponse with application status and details
     */
    suspend fun getApplicationStatus(applicationId: String): LoanResponse
}
