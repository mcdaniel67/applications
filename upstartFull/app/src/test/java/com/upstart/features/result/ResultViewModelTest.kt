package com.upstart.features.result

import app.cash.turbine.test
import com.upstart.core.models.LoanResponse
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
import kotlin.test.assertIs

@OptIn(ExperimentalCoroutinesApi::class)
class ResultViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `fetchApplicationStatus emits error when id is missing`() = runTest {
        val repository = TestLoanRepository()
        val viewModel = ResultViewModel(repository = repository)

        viewModel.state.test {
            assertIs<ResultState.Loading>(awaitItem())

            viewModel.fetchApplicationStatus(null)

            val error = awaitItem()
            assertIs<ResultState.Error>(error)
            assertEquals("No application ID found", error.message)
        }
    }

    @Test
    fun `fetchApplicationStatus emits success when repository succeeds`() = runTest {
        val expectedResponse = LoanResponse(
            success = true,
            message = "Approved",
            applicationId = "APP-321"
        )
        val repository = TestLoanRepository().apply {
            getApplicationStatusResult = Result.success(expectedResponse)
        }
        val viewModel = ResultViewModel(repository = repository)

        viewModel.state.test {
            assertIs<ResultState.Loading>(awaitItem())

            viewModel.fetchApplicationStatus("APP-321")
            testDispatcher.scheduler.advanceUntilIdle()

            val success = awaitItem()
            val successState = assertIs<ResultState.Success>(success)
            assertEquals(expectedResponse, successState.result)
            assertEquals("APP-321", repository.requestedApplicationId)
        }
    }

    @Test
    fun `fetchApplicationStatus emits error when repository fails`() = runTest {
        val repository = TestLoanRepository().apply {
            getApplicationStatusResult = Result.failure(IllegalStateException("server down"))
        }
        val viewModel = ResultViewModel(repository = repository)

        viewModel.state.test {
            assertIs<ResultState.Loading>(awaitItem())

            viewModel.fetchApplicationStatus("APP-777")
            testDispatcher.scheduler.advanceUntilIdle()

            val error = awaitItem()
            val errorState = assertIs<ResultState.Error>(error)
            assertEquals("server down", errorState.message)
        }
    }

    @Test
    fun `reset brings state back to loading`() = runTest {
        val repository = TestLoanRepository().apply {
            getApplicationStatusResult = Result.success(
                LoanResponse(
                    success = true,
                    message = "done",
                    applicationId = "APP-42"
                )
            )
        }
        val viewModel = ResultViewModel(repository = repository)

        viewModel.fetchApplicationStatus("APP-42")
        testDispatcher.scheduler.advanceUntilIdle()
        assertIs<ResultState.Success>(viewModel.state.value)

        viewModel.reset()

        assertIs<ResultState.Loading>(viewModel.state.value)
    }
}
