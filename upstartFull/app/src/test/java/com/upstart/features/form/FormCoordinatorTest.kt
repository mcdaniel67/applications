package com.upstart.features.form

import app.cash.turbine.test
import com.upstart.core.models.LoanType
import com.upstart.features.loanDetails.LoanDetailsIntent
import com.upstart.features.loanDetails.LoanDetailsViewModel
import com.upstart.features.personalInfo.PersonalInfoIntent
import com.upstart.features.personalInfo.PersonalInfoViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class FormCoordinatorTest {

    private lateinit var coordinator: FormCoordinator
    private lateinit var loanDetailsViewModel: LoanDetailsViewModel
    private lateinit var personalInfoViewModel: PersonalInfoViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        loanDetailsViewModel = LoanDetailsViewModel()
        personalInfoViewModel = PersonalInfoViewModel()
        coordinator = FormCoordinator(loanDetailsViewModel, personalInfoViewModel)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state starts at LOAN_DETAILS step`() = runTest {
        coordinator.state.test {
            val state = awaitItem()
            assertEquals(FormStep.LOAN_DETAILS, state.currentStep)
            assertFalse(state.isLoadingStep)
            assertNull(state.result)
            assertFalse(state.canGoBack)
            assertTrue(state.isLastStep == false)
        }
    }

    @Test
    fun `NextStep navigates to next step when current step is valid`() = runTest {
        // Setup valid loan details
        loanDetailsViewModel.handleIntent(LoanDetailsIntent.UpdateAmount(10000f))
        loanDetailsViewModel.handleIntent(LoanDetailsIntent.UpdateLoanType(LoanType.PERSONAL))
        testDispatcher.scheduler.advanceUntilIdle()

        coordinator.state.test {
            skipItems(1) // Skip initial state

            coordinator.handleIntent(FormIntent.NextStep)

            // First emission: isLoadingStep = true
            var state = awaitItem()
            assertTrue(state.isLoadingStep)

            // Advance past the submission delay
            testDispatcher.scheduler.advanceTimeBy(2000)
            testDispatcher.scheduler.advanceUntilIdle()

            // Second emission: navigated to next step
            state = awaitItem()
            assertEquals(FormStep.PERSONAL_INFO, state.currentStep)
            assertFalse(state.isLoadingStep)
            assertTrue(state.canGoBack)
        }
    }

    @Test
    fun `NextStep does not navigate when current step is invalid`() = runTest {
        coordinator.state.test {
            val initialState = awaitItem()
            assertEquals(FormStep.LOAN_DETAILS, initialState.currentStep)

            coordinator.handleIntent(FormIntent.NextStep)
            testDispatcher.scheduler.advanceUntilIdle()

            // Should still be on LOAN_DETAILS because validation failed
            expectNoEvents()
        }
    }

    @Test
    fun `NextStep does not navigate from RESULT step`() = runTest {
        // Setup valid form and submit
        setupValidForm()
        coordinator.handleIntent(FormIntent.Submit)
        testDispatcher.scheduler.advanceUntilIdle()
        advanceTimeBy(2000) // Wait for submission delay

        coordinator.state.test {
            val state = awaitItem()
            assertEquals(FormStep.RESULT, state.currentStep)

            coordinator.handleIntent(FormIntent.NextStep)
            testDispatcher.scheduler.advanceUntilIdle()

            // Should still be on RESULT
            expectNoEvents()
        }
    }

    @Test
    fun `PreviousStep navigates to previous step`() = runTest {
        // Navigate to PERSONAL_INFO first
        setupValidLoanDetails()
        coordinator.handleIntent(FormIntent.NextStep)
        testDispatcher.scheduler.advanceUntilIdle()

        coordinator.state.test {
            skipItems(1) // Skip current state

            coordinator.handleIntent(FormIntent.PreviousStep)
            testDispatcher.scheduler.advanceUntilIdle()

            val state = awaitItem()
            assertEquals(FormStep.LOAN_DETAILS, state.currentStep)
            assertFalse(state.canGoBack)
        }
    }

    @Test
    fun `PreviousStep does not navigate from first step`() = runTest {
        coordinator.state.test {
            val initialState = awaitItem()
            assertEquals(FormStep.LOAN_DETAILS, initialState.currentStep)

            coordinator.handleIntent(FormIntent.PreviousStep)
            testDispatcher.scheduler.advanceUntilIdle()

            // Should still be on first step
            expectNoEvents()
        }
    }

    @Test
    fun `Submit navigates to RESULT step with valid form data`() = runTest {
        setupValidForm()

        coordinator.state.test {
            skipItems(1) // Skip initial state

            coordinator.handleIntent(FormIntent.Submit)
            testDispatcher.scheduler.advanceUntilIdle()

            // Should navigate to RESULT step
            val resultState = awaitItem()
            assertEquals(FormStep.RESULT, resultState.currentStep)
        }
    }

    @Test
    fun `Submit fails when loan details are invalid`() = runTest {
        // Only setup personal info, leave loan details invalid
        personalInfoViewModel.handleIntent(PersonalInfoIntent.UpdateFirstName("John"))
        personalInfoViewModel.handleIntent(PersonalInfoIntent.UpdateLastName("Doe"))
        personalInfoViewModel.handleIntent(PersonalInfoIntent.UpdateEmail("john@example.com"))
        testDispatcher.scheduler.advanceUntilIdle()

        coordinator.state.test {
            skipItems(1) // Skip initial state

            coordinator.handleIntent(FormIntent.Submit)
            testDispatcher.scheduler.advanceUntilIdle()

            val state = awaitItem()
            assertNotNull(state.result)
            assertFalse(state.result!!.success)
            assertEquals("Please fix all validation errors", state.result!!.message)
        }
    }

    @Test
    fun `Submit fails when personal info is invalid`() = runTest {
        // Only setup loan details, leave personal info invalid
        setupValidLoanDetails()

        coordinator.state.test {
            skipItems(1) // Skip initial state

            coordinator.handleIntent(FormIntent.Submit)
            testDispatcher.scheduler.advanceUntilIdle()

            val state = awaitItem()
            assertNotNull(state.result)
            assertFalse(state.result!!.success)
            assertEquals("Please fix all validation errors", state.result!!.message)
        }
    }

    @Test
    fun `Reset returns to initial state`() = runTest {
        // Setup and navigate through form
        setupValidForm()
        coordinator.handleIntent(FormIntent.NextStep)
        testDispatcher.scheduler.advanceUntilIdle()

        coordinator.state.test {
            skipItems(1) // Skip current state

            coordinator.handleIntent(FormIntent.Reset)
            testDispatcher.scheduler.advanceUntilIdle()

            val state = awaitItem()
            assertEquals(FormStep.LOAN_DETAILS, state.currentStep)
            assertFalse(state.isLoadingStep)
            assertNull(state.result)
            assertFalse(state.canGoBack)
        }
    }

    @Test
    fun `canProceed returns true only when current step is valid`() = runTest {
        // Initially true (validations haven't run yet)
        assertTrue(coordinator.canProceed())

        // Trigger validation to make it fail (no loan type selected)
        loanDetailsViewModel.validateAll()
        testDispatcher.scheduler.advanceUntilIdle()
        assertFalse(coordinator.canProceed())

        // Setup valid loan details
        setupValidLoanDetails()
        assertTrue(coordinator.canProceed())

        // Navigate to personal info
        coordinator.handleIntent(FormIntent.NextStep)
        testDispatcher.scheduler.advanceUntilIdle()

        // True on personal info (validations haven't run)
        assertTrue(coordinator.canProceed())

        // Trigger validation to make it fail
        personalInfoViewModel.validateAll()
        testDispatcher.scheduler.advanceUntilIdle()
        assertFalse(coordinator.canProceed())

        // Setup valid personal info
        setupValidPersonalInfo()
        assertTrue(coordinator.canProceed())
    }

    @Test
    fun `canProceed returns false on RESULT step`() = runTest {
        setupValidForm()
        coordinator.handleIntent(FormIntent.Submit)
        testDispatcher.scheduler.advanceUntilIdle()
        advanceTimeBy(2000)

        assertFalse(coordinator.canProceed())
    }

    @Test
    fun `isLastStep is true only on PERSONAL_INFO step`() = runTest {
        coordinator.state.test {
            // LOAN_DETAILS - not last step
            var state = awaitItem()
            assertFalse(state.isLastStep)

            // Navigate to PERSONAL_INFO - is last step
            setupValidLoanDetails()
            coordinator.handleIntent(FormIntent.NextStep)
            state = awaitItem() // isLoadingStep = true
            testDispatcher.scheduler.advanceTimeBy(2000)
            testDispatcher.scheduler.advanceUntilIdle()
            state = awaitItem() // navigated to PERSONAL_INFO
            assertTrue(state.isLastStep)

            // Navigate to RESULT - not last step (but can't proceed anyway)
            setupValidPersonalInfo()
            coordinator.handleIntent(FormIntent.Submit)
            testDispatcher.scheduler.advanceUntilIdle()
            state = awaitItem() // Navigate to RESULT
            assertFalse(state.isLastStep)
        }
    }

    @Test
    fun `canGoBack is false on LOAN_DETAILS and RESULT steps`() = runTest {
        coordinator.state.test {
            // LOAN_DETAILS - can't go back
            var state = awaitItem()
            assertFalse(state.canGoBack)

            // PERSONAL_INFO - can go back
            setupValidLoanDetails()
            coordinator.handleIntent(FormIntent.NextStep)
            state = awaitItem() // isLoadingStep = true
            testDispatcher.scheduler.advanceTimeBy(2000)
            testDispatcher.scheduler.advanceUntilIdle()
            state = awaitItem() // navigated to PERSONAL_INFO
            assertTrue(state.canGoBack)

            // RESULT - can't go back
            setupValidPersonalInfo()
            coordinator.handleIntent(FormIntent.Submit)
            testDispatcher.scheduler.advanceUntilIdle()
            state = awaitItem() // Navigate to RESULT
            assertFalse(state.canGoBack)
        }
    }

    @Test
    fun `full form flow from start to result`() = runTest {
        coordinator.state.test {
            // Start at LOAN_DETAILS
            var state = awaitItem()
            assertEquals(FormStep.LOAN_DETAILS, state.currentStep)

            // Fill in loan details
            setupValidLoanDetails()
            coordinator.handleIntent(FormIntent.NextStep)

            // Loading step
            state = awaitItem()
            assertTrue(state.isLoadingStep)

            // Advance past submission delay
            testDispatcher.scheduler.advanceTimeBy(2000)
            testDispatcher.scheduler.advanceUntilIdle()

            // Now at PERSONAL_INFO
            state = awaitItem()
            assertEquals(FormStep.PERSONAL_INFO, state.currentStep)

            // Fill in personal info
            setupValidPersonalInfo()
            coordinator.handleIntent(FormIntent.Submit)
            testDispatcher.scheduler.advanceUntilIdle()

            // Now at RESULT
            state = awaitItem()
            assertEquals(FormStep.RESULT, state.currentStep)

            // Reset
            coordinator.handleIntent(FormIntent.Reset)
            testDispatcher.scheduler.advanceUntilIdle()

            // Back to LOAN_DETAILS
            state = awaitItem()
            assertEquals(FormStep.LOAN_DETAILS, state.currentStep)
            assertNull(state.result)
        }
    }

    // Helper methods
    private fun setupValidLoanDetails() {
        loanDetailsViewModel.handleIntent(LoanDetailsIntent.UpdateAmount(10000f))
        loanDetailsViewModel.handleIntent(LoanDetailsIntent.UpdateLoanType(LoanType.PERSONAL))
        testDispatcher.scheduler.advanceUntilIdle()
    }

    private fun setupValidPersonalInfo() {
        personalInfoViewModel.handleIntent(PersonalInfoIntent.UpdateFirstName("John"))
        personalInfoViewModel.handleIntent(PersonalInfoIntent.UpdateLastName("Doe"))
        personalInfoViewModel.handleIntent(PersonalInfoIntent.UpdateEmail("john@example.com"))
        testDispatcher.scheduler.advanceUntilIdle()
    }

    private fun setupValidForm() {
        setupValidLoanDetails()
        setupValidPersonalInfo()
    }
}
