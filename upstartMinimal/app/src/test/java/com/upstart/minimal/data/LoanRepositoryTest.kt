package com.upstart.minimal.data

import com.upstart.minimal.data.remote.MockLoanApi
import com.upstart.minimal.domain.model.LoanSubmissionRequest
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LoanRepositoryTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun submitLoanApplication_parsesSuccessJson() = runTest {
        val repository = LoanRepository(MockLoanApi(json), json)
        val request = LoanSubmissionRequest(
            purposeId = "credit_card",
            amount = 14000,
            borrowerName = "Alex"
        )

        val result = repository.submitLoanApplication(request).getOrThrow()

        assertTrue(result.referenceId.startsWith("UP-"))
        assertTrue(result.approved)
        assertEquals(
            "Congrats Alex, your 14000 loan is provisionally approved.",
            result.message
        )
    }

    @Test
    fun submitLoanApplication_handlesManualReviewResponse() = runTest {
        val repository = LoanRepository(MockLoanApi(json), json)
        val request = LoanSubmissionRequest(
            purposeId = "home_improvement",
            amount = 19000,
            borrowerName = "Morgan"
        )

        val result = repository.submitLoanApplication(request).getOrThrow()

        assertTrue(result.referenceId.startsWith("UP-"))
        assertFalse(result.approved)
        assertEquals(
            "Thanks Morgan, we need a manual review for your request.",
            result.message
        )
    }
}
