package com.upstart.features.result

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.upstart.core.models.LoanResponse
import com.upstart.data.repository.FakeLoanRepository
import com.upstart.data.repository.LoanRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.IOException

sealed class ResultState {
    data object Loading : ResultState()
    data class Success(val result: LoanResponse) : ResultState()
    data class Error(val message: String) : ResultState()
}

class ResultViewModel(
    private val repository: LoanRepository = FakeLoanRepository()
) : ViewModel() {

    private val _state = MutableStateFlow<ResultState>(ResultState.Loading)
    val state: StateFlow<ResultState> = _state.asStateFlow()

    fun fetchApplicationStatus(applicationId: String?) {
        if (applicationId == null) {
            _state.value = ResultState.Error("No application ID found")
            return
        }

        viewModelScope.launch {
            _state.value = ResultState.Loading
            try {
                val response = repository.getApplicationStatus(applicationId)
                _state.value = ResultState.Success(response)
            } catch (e: IOException) {
                _state.value = ResultState.Error(e.message ?: "Failed to fetch application status")
            }
        }
    }

    fun reset() {
        _state.value = ResultState.Loading
    }
}
