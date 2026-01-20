package com.upstart.data.repository

import com.upstart.core.models.LoanResponse
import com.upstart.core.models.LoanType
import kotlinx.coroutines.delay
import java.io.IOException
import kotlin.time.Duration.Companion.seconds

/**
 * Fake implementation of LoanRepository that simulates API calls.
 * Used for development and testing.
 */
class FakeLoanRepository : LoanRepository {

    override suspend fun submitLoanDetails(amount: Float, loanType: LoanType): String {
        // Simulate network delay
        delay(2.seconds)

        // Simulate potential failure (10% chance)
        if (Math.random() < 0.1) {
            throw IOException("Network error: Failed to submit loan details")
        }

        // Return a UUID for the application
        return "APP-${System.currentTimeMillis()}"
    }

    override suspend fun submitPersonalInfo(firstName: String, lastName: String, email: String): String {
        // Simulate network delay
        delay(1.seconds)

        // Simulate potential failure (10% chance)
        if (Math.random() < 0.1) {
            throw IOException("Network error: Failed to submit personal information")
        }

        return "Personal information saved for $firstName $lastName"
    }

    override suspend fun getApplicationStatus(applicationId: String): LoanResponse {
        // Simulate network delay
        delay(2.seconds)

        return LoanResponse(
            success = true,
            message = "Your loan application has been submitted successfully!",
            applicationId = applicationId
        )
    }
}
