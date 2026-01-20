package com.upstart.features.loanDetails

import app.cash.turbine.test
import com.upstart.core.form.SubmissionState
import com.upstart.core.models.LoanType
import com.upstart.data.repository.TestLoanRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class LoanDetailsViewModelTest {

    private lateinit var viewModel: LoanDetailsViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = LoanDetailsViewModel()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state has default values`() = runTest {
        viewModel.state.test {
            val state = awaitItem()
            assertEquals(5000f, state.amount)
            assertEquals(null, state.loanType)
            assertTrue(state.amountValidation.isValid)
            assertTrue(state.loanTypeValidation.isValid)
            assertTrue(state.isValid) // Validations haven't run yet, so all show as valid
        }
    }

    @Test
    fun `UpdateAmount intent updates amount and validates`() = runTest {
        viewModel.state.test {
            skipItems(1) // Skip initial state

            viewModel.handleIntent(LoanDetailsIntent.UpdateAmount(10000f))
            testDispatcher.scheduler.advanceUntilIdle()

            val state = awaitItem()
            assertEquals(10000f, state.amount)
            assertTrue(state.amountValidation.isValid)
        }
    }

    @Test
    fun `UpdateAmount intent with amount below minimum fails validation`() = runTest {
        viewModel.state.test {
            skipItems(1) // Skip initial state

            viewModel.handleIntent(LoanDetailsIntent.UpdateAmount(500f))
            testDispatcher.scheduler.advanceUntilIdle()

            val state = awaitItem()
            assertEquals(500f, state.amount)
            assertFalse(state.amountValidation.isValid)
            assertEquals("Minimum loan amount is 1000", state.amountValidation.errorMessage)
        }
    }

    @Test
    fun `UpdateAmount intent with amount above maximum fails validation`() = runTest {
        viewModel.state.test {
            skipItems(1) // Skip initial state

            viewModel.handleIntent(LoanDetailsIntent.UpdateAmount(60000f))
            testDispatcher.scheduler.advanceUntilIdle()

            val state = awaitItem()
            assertEquals(60000f, state.amount)
            assertFalse(state.amountValidation.isValid)
            assertEquals("Maximum loan amount is 50000", state.amountValidation.errorMessage)
        }
    }

    @Test
    fun `UpdateLoanType intent updates loan type`() = runTest {
        viewModel.state.test {
            skipItems(1) // Skip initial state

            viewModel.handleIntent(LoanDetailsIntent.UpdateLoanType(LoanType.AUTO))
            testDispatcher.scheduler.advanceUntilIdle()

            val state = awaitItem()
            assertEquals(LoanType.AUTO, state.loanType)
            assertTrue(state.loanTypeValidation.isValid)
        }
    }

    @Test
    fun `validateAll returns true when all fields are valid`() = runTest {
        viewModel.handleIntent(LoanDetailsIntent.UpdateAmount(10000f))
        viewModel.handleIntent(LoanDetailsIntent.UpdateLoanType(LoanType.PERSONAL))
        testDispatcher.scheduler.advanceUntilIdle()

        val result = viewModel.validateAll()
        assertTrue(result)

        viewModel.state.test {
            val state = awaitItem()
            assertTrue(state.isValid)
            assertTrue(state.amountValidation.isValid)
            assertTrue(state.loanTypeValidation.isValid)
        }
    }

    @Test
    fun `validateAll returns false when loan type is not selected`() = runTest {
        viewModel.handleIntent(LoanDetailsIntent.UpdateAmount(10000f))
        testDispatcher.scheduler.advanceUntilIdle()

        val result = viewModel.validateAll()
        assertFalse(result)

        viewModel.state.test {
            val state = awaitItem()
            assertFalse(state.isValid)
            assertFalse(state.loanTypeValidation.isValid)
            assertEquals("Please select a loan type", state.loanTypeValidation.errorMessage)
        }
    }

    @Test
    fun `validateAll returns false when amount is out of range`() = runTest {
        viewModel.handleIntent(LoanDetailsIntent.UpdateAmount(100f))
        viewModel.handleIntent(LoanDetailsIntent.UpdateLoanType(LoanType.MORTGAGE))
        testDispatcher.scheduler.advanceUntilIdle()

        val result = viewModel.validateAll()
        assertFalse(result)

        viewModel.state.test {
            val state = awaitItem()
            assertFalse(state.isValid)
            assertFalse(state.amountValidation.isValid)
        }
    }

    @Test
    fun `state isValid is true only when both fields are valid`() = runTest {
        viewModel.state.test {
            // Initial state - valid (validations haven't run)
            var state = awaitItem()
            assertTrue(state.isValid)

            // Add valid amount - still valid
            viewModel.handleIntent(LoanDetailsIntent.UpdateAmount(10000f))
            testDispatcher.scheduler.advanceUntilIdle()
            state = awaitItem()
            assertTrue(state.isValid)

            // Add loan type - still valid
            viewModel.handleIntent(LoanDetailsIntent.UpdateLoanType(LoanType.AUTO))
            testDispatcher.scheduler.advanceUntilIdle()
            state = awaitItem()
            assertTrue(state.isValid)

            // Update with invalid amount - now invalid
            viewModel.handleIntent(LoanDetailsIntent.UpdateAmount(500f))
            testDispatcher.scheduler.advanceUntilIdle()
            state = awaitItem()
            assertFalse(state.isValid)
        }
    }

    @Test
    fun `multiple loan types can be selected`() = runTest {
        val loanTypes = listOf(
            LoanType.AUTO,
            LoanType.MORTGAGE,
            LoanType.PERSONAL,
            LoanType.STUDENT,
            LoanType.BUSINESS,
            LoanType.HOME_IMPROVEMENT
        )

        viewModel.handleIntent(LoanDetailsIntent.UpdateAmount(10000f))
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.state.test {
            skipItems(1) // Skip current state

            loanTypes.forEach { loanType ->
                viewModel.handleIntent(LoanDetailsIntent.UpdateLoanType(loanType))
                testDispatcher.scheduler.advanceUntilIdle()

                val state = awaitItem()
                assertEquals(loanType, state.loanType)
                assertTrue(state.loanTypeValidation.isValid)
            }
        }
    }

    @Test
    fun `submit returns error when validation fails`() = runTest {
        val repository = TestLoanRepository()
        val viewModel = LoanDetailsViewModel(repository = repository)

        // Only set amount, leave loan type blank to trigger validation error
        viewModel.handleIntent(LoanDetailsIntent.UpdateAmount(1500f))
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.submit()

        val submissionState = viewModel.submissionState.value
        val errorState = assertIs<SubmissionState.Error>(submissionState)
        assertEquals("Please fix validation errors before submitting", errorState.message)
        assertNull(repository.submittedLoanDetails, "Repository should not be called when validation fails")
    }

    @Test
    fun `submit stores application id when repository succeeds`() = runTest {
        val repository = TestLoanRepository().apply {
            submitLoanDetailsResult = Result.success("APP-999")
        }
        val viewModel = LoanDetailsViewModel(repository = repository)

        viewModel.handleIntent(LoanDetailsIntent.UpdateAmount(25000f))
        viewModel.handleIntent(LoanDetailsIntent.UpdateLoanType(LoanType.STUDENT))
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.submit()

        val successState = assertIs<SubmissionState.Success>(viewModel.submissionState.value)
        assertEquals("Loan details saved with ID: APP-999", successState.message)
        assertEquals("APP-999", viewModel.state.value.applicationId)
        assertEquals(25000f, repository.submittedLoanDetails?.first)
        assertEquals(LoanType.STUDENT, repository.submittedLoanDetails?.second)
    }

    @Test
    fun `submit surfaces repository failures`() = runTest {
        val repository = TestLoanRepository().apply {
            submitLoanDetailsResult = Result.failure(IllegalStateException("boom"))
        }
        val viewModel = LoanDetailsViewModel(repository = repository)

        viewModel.handleIntent(LoanDetailsIntent.UpdateAmount(5000f))
        viewModel.handleIntent(LoanDetailsIntent.UpdateLoanType(LoanType.AUTO))
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.submit()

        val errorState = assertIs<SubmissionState.Error>(viewModel.submissionState.value)
        assertEquals("boom", errorState.message)
    }
}
