package com.upstart.features.personalInfo

import app.cash.turbine.test
import com.upstart.core.form.SubmissionState
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
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class PersonalInfoViewModelTest {

    private lateinit var viewModel: PersonalInfoViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = PersonalInfoViewModel()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state has empty values`() = runTest {
        viewModel.state.test {
            val state = awaitItem()
            assertEquals("", state.firstName)
            assertEquals("", state.lastName)
            assertEquals("", state.email)
            assertTrue(state.firstNameValidation.isValid)
            assertTrue(state.lastNameValidation.isValid)
            assertTrue(state.emailValidation.isValid)
            assertTrue(state.isValid)
        }
    }

    @Test
    fun `UpdateFirstName intent updates first name and validates`() = runTest {
        viewModel.state.test {
            skipItems(1) // Skip initial state

            viewModel.handleIntent(PersonalInfoIntent.UpdateFirstName("John"))
            testDispatcher.scheduler.advanceUntilIdle()

            val state = awaitItem()
            assertEquals("John", state.firstName)
            assertTrue(state.firstNameValidation.isValid)
        }
    }

    @Test
    fun `UpdateFirstName intent with short name fails validation`() = runTest {
        viewModel.state.test {
            skipItems(1) // Skip initial state

            viewModel.handleIntent(PersonalInfoIntent.UpdateFirstName("J"))
            testDispatcher.scheduler.advanceUntilIdle()

            val state = awaitItem()
            assertEquals("J", state.firstName)
            assertFalse(state.firstNameValidation.isValid)
            assertEquals("First name must be at least 2 characters", state.firstNameValidation.errorMessage)
        }
    }

    @Test
    fun `UpdateLastName intent updates last name and validates`() = runTest {
        viewModel.state.test {
            skipItems(1) // Skip initial state

            viewModel.handleIntent(PersonalInfoIntent.UpdateLastName("Doe"))
            testDispatcher.scheduler.advanceUntilIdle()

            val state = awaitItem()
            assertEquals("Doe", state.lastName)
            assertTrue(state.lastNameValidation.isValid)
        }
    }

    @Test
    fun `UpdateLastName intent with short name fails validation`() = runTest {
        viewModel.state.test {
            skipItems(1) // Skip initial state

            viewModel.handleIntent(PersonalInfoIntent.UpdateLastName("D"))
            testDispatcher.scheduler.advanceUntilIdle()

            val state = awaitItem()
            assertEquals("D", state.lastName)
            assertFalse(state.lastNameValidation.isValid)
            assertEquals("Last name must be at least 2 characters", state.lastNameValidation.errorMessage)
        }
    }

    @Test
    fun `UpdateEmail intent updates email and validates`() = runTest {
        viewModel.state.test {
            skipItems(1) // Skip initial state

            viewModel.handleIntent(PersonalInfoIntent.UpdateEmail("test@example.com"))
            testDispatcher.scheduler.advanceUntilIdle()

            val state = awaitItem()
            assertEquals("test@example.com", state.email)
            assertTrue(state.emailValidation.isValid)
        }
    }

    @Test
    fun `UpdateEmail intent with invalid email fails validation`() = runTest {
        viewModel.state.test {
            skipItems(1) // Skip initial state

            viewModel.handleIntent(PersonalInfoIntent.UpdateEmail("invalid-email"))
            testDispatcher.scheduler.advanceUntilIdle()

            val state = awaitItem()
            assertEquals("invalid-email", state.email)
            assertFalse(state.emailValidation.isValid)
            assertEquals("Please enter a valid email", state.emailValidation.errorMessage)
        }
    }

    @Test
    fun `validateAll returns true when all fields are valid`() = runTest {
        viewModel.handleIntent(PersonalInfoIntent.UpdateFirstName("John"))
        viewModel.handleIntent(PersonalInfoIntent.UpdateLastName("Doe"))
        viewModel.handleIntent(PersonalInfoIntent.UpdateEmail("john.doe@example.com"))
        testDispatcher.scheduler.advanceUntilIdle()

        val result = viewModel.validateAll()
        assertTrue(result)

        viewModel.state.test {
            val state = awaitItem()
            assertTrue(state.isValid)
            assertTrue(state.firstNameValidation.isValid)
            assertTrue(state.lastNameValidation.isValid)
            assertTrue(state.emailValidation.isValid)
        }
    }

    @Test
    fun `validateAll returns false when first name is too short`() = runTest {
        viewModel.handleIntent(PersonalInfoIntent.UpdateFirstName("J"))
        viewModel.handleIntent(PersonalInfoIntent.UpdateLastName("Doe"))
        viewModel.handleIntent(PersonalInfoIntent.UpdateEmail("john@example.com"))
        testDispatcher.scheduler.advanceUntilIdle()

        val result = viewModel.validateAll()
        assertFalse(result)

        viewModel.state.test {
            val state = awaitItem()
            assertFalse(state.isValid)
            assertFalse(state.firstNameValidation.isValid)
        }
    }

    @Test
    fun `validateAll returns false when last name is too short`() = runTest {
        viewModel.handleIntent(PersonalInfoIntent.UpdateFirstName("John"))
        viewModel.handleIntent(PersonalInfoIntent.UpdateLastName("D"))
        viewModel.handleIntent(PersonalInfoIntent.UpdateEmail("john@example.com"))
        testDispatcher.scheduler.advanceUntilIdle()

        val result = viewModel.validateAll()
        assertFalse(result)

        viewModel.state.test {
            val state = awaitItem()
            assertFalse(state.isValid)
            assertFalse(state.lastNameValidation.isValid)
        }
    }

    @Test
    fun `validateAll returns false when email is invalid`() = runTest {
        viewModel.handleIntent(PersonalInfoIntent.UpdateFirstName("John"))
        viewModel.handleIntent(PersonalInfoIntent.UpdateLastName("Doe"))
        viewModel.handleIntent(PersonalInfoIntent.UpdateEmail("invalid"))
        testDispatcher.scheduler.advanceUntilIdle()

        val result = viewModel.validateAll()
        assertFalse(result)

        viewModel.state.test {
            val state = awaitItem()
            assertFalse(state.isValid)
            assertFalse(state.emailValidation.isValid)
        }
    }

    @Test
    fun `validateAll returns false when fields are empty`() = runTest {
        val result = viewModel.validateAll()
        assertFalse(result)

        viewModel.state.test {
            val state = awaitItem()
            assertFalse(state.isValid)
            assertFalse(state.firstNameValidation.isValid)
            assertFalse(state.lastNameValidation.isValid)
            assertFalse(state.emailValidation.isValid)
        }
    }

    @Test
    fun `state isValid is true only when all fields are valid`() = runTest {
        viewModel.state.test {
            // Initial state - valid (no validations run yet)
            var state = awaitItem()
            assertTrue(state.isValid)

            // Add first name
            viewModel.handleIntent(PersonalInfoIntent.UpdateFirstName("John"))
            testDispatcher.scheduler.advanceUntilIdle()
            state = awaitItem()
            assertTrue(state.isValid) // Still valid with just first name

            // Add last name
            viewModel.handleIntent(PersonalInfoIntent.UpdateLastName("Doe"))
            testDispatcher.scheduler.advanceUntilIdle()
            state = awaitItem()
            assertTrue(state.isValid) // Still valid with first and last name

            // Add valid email - all valid
            viewModel.handleIntent(PersonalInfoIntent.UpdateEmail("john@example.com"))
            testDispatcher.scheduler.advanceUntilIdle()
            state = awaitItem()
            assertTrue(state.isValid)

            // Change email to invalid
            viewModel.handleIntent(PersonalInfoIntent.UpdateEmail("invalid"))
            testDispatcher.scheduler.advanceUntilIdle()
            state = awaitItem()
            assertFalse(state.isValid)
        }
    }

    @Test
    fun `valid email formats are accepted`() = runTest {
        val validEmails = listOf(
            "user@example.com",
            "user.name@example.com",
            "user+tag@example.co.uk",
            "user_name@example.com"
        )

        viewModel.state.test {
            skipItems(1) // Skip initial state

            validEmails.forEach { email ->
                viewModel.handleIntent(PersonalInfoIntent.UpdateEmail(email))
                testDispatcher.scheduler.advanceUntilIdle()

                val state = awaitItem()
                assertTrue(state.emailValidation.isValid, "Expected $email to be valid")
            }
        }
    }

    @Test
    fun `submit returns error when validation fails`() = runTest {
        val repository = TestLoanRepository()
        val viewModel = PersonalInfoViewModel(repository = repository)

        viewModel.submit()

        val errorState = assertIs<SubmissionState.Error>(viewModel.submissionState.value)
        assertEquals("Please fix validation errors before submitting", errorState.message)
        assertEquals(null, repository.submittedPersonalInfo, "Repository should not be called when form is invalid")
    }

    @Test
    fun `submit returns success message from repository`() = runTest {
        val repository = TestLoanRepository().apply {
            submitPersonalInfoResult = Result.success("Saved personal info")
        }
        val viewModel = PersonalInfoViewModel(repository = repository)

        viewModel.handleIntent(PersonalInfoIntent.UpdateFirstName("Jane"))
        viewModel.handleIntent(PersonalInfoIntent.UpdateLastName("Doe"))
        viewModel.handleIntent(PersonalInfoIntent.UpdateEmail("jane@example.com"))
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.submit()

        val successState = assertIs<SubmissionState.Success>(viewModel.submissionState.value)
        assertEquals("Saved personal info", successState.message)
        assertEquals(
            Triple("Jane", "Doe", "jane@example.com"),
            repository.submittedPersonalInfo
        )
    }

    @Test
    fun `submit surfaces repository failure`() = runTest {
        val repository = TestLoanRepository().apply {
            submitPersonalInfoResult = Result.failure(IllegalStateException("network boom"))
        }
        val viewModel = PersonalInfoViewModel(repository = repository)

        viewModel.handleIntent(PersonalInfoIntent.UpdateFirstName("Jane"))
        viewModel.handleIntent(PersonalInfoIntent.UpdateLastName("Doe"))
        viewModel.handleIntent(PersonalInfoIntent.UpdateEmail("jane@example.com"))
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.submit()

        val errorState = assertIs<SubmissionState.Error>(viewModel.submissionState.value)
        assertEquals("network boom", errorState.message)
    }
}
