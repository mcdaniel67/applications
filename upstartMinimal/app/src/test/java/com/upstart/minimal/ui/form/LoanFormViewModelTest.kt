package com.upstart.minimal.ui.form

import com.upstart.minimal.MainDispatcherRule
import com.upstart.minimal.data.LoanRepository
import com.upstart.minimal.data.remote.LoanApi
import com.upstart.minimal.data.remote.dto.LoanSubmissionRequestDto
import com.upstart.minimal.data.remote.dto.LoanSubmissionResponseDto
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LoanFormViewModelTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun loanAmountChange_snapsToNearestStep() {
        val viewModel = createViewModel(FakeLoanApi())

        viewModel.onEvent(LoanFormEvent.LoanAmountChanged(14499f))

        assertEquals(14500f, viewModel.state.value.loanAmount)
    }

    @Test
    fun submit_withoutBorrowerName_setsError() {
        val fakeApi = FakeLoanApi()
        val viewModel = createViewModel(fakeApi)

        viewModel.onEvent(LoanFormEvent.Submit)

        assertNotNull(viewModel.state.value.borrowerNameError)
        assertEquals(0, fakeApi.requestCount)
    }

    @Test
    fun submit_success_updatesResult() = runTest {
        val response = LoanSubmissionResponseDto(
            confirmationId = "UP-12345",
            approved = true,
            message = "All good"
        )
        val fakeApi = FakeLoanApi(responseJson = json.encodeToString(response))
        val viewModel = createViewModel(fakeApi)

        viewModel.onEvent(LoanFormEvent.BorrowerNameChanged("Riley"))
        viewModel.onEvent(LoanFormEvent.LoanAmountChanged(12550f))
        viewModel.onEvent(LoanFormEvent.Submit)
        advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals("UP-12345", state.submissionResult?.referenceId)
        assertNull(state.errorMessage)
        val capturedRequest = fakeApi.lastRequest
        assertNotNull(capturedRequest)
        val parsed = json.decodeFromString<LoanSubmissionRequestDto>(capturedRequest!!)
        assertEquals(12500, parsed.amount)
        assertEquals("Riley", parsed.borrowerName)
    }

    @Test
    fun submit_failure_setsErrorMessage() = runTest {
        val fakeApi = FakeLoanApi(error = IllegalStateException("network down"))
        val viewModel = createViewModel(fakeApi)

        viewModel.onEvent(LoanFormEvent.BorrowerNameChanged("Chris"))
        viewModel.onEvent(LoanFormEvent.Submit)
        advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals("network down", state.errorMessage)
        assertNull(state.submissionResult)
    }

    private fun createViewModel(api: LoanApi): LoanFormViewModel {
        val repository = LoanRepository(api, json)
        return LoanFormViewModel(repository)
    }

    private class FakeLoanApi(
        private val responseJson: String? = null,
        private val error: Throwable? = null
    ) : LoanApi {

        var lastRequest: String? = null
            private set
        var requestCount: Int = 0
            private set

        override suspend fun submitLoanApplication(requestJson: String): String {
            requestCount++
            lastRequest = requestJson
            error?.let { throw it }
            return responseJson
                ?: error("FakeLoanApi requires responseJson when error is null")
        }
    }
}
