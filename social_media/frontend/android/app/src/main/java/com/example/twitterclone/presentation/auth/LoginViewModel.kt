package com.example.twitterclone.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.twitterclone.domain.repository.AuthRepository
import com.example.twitterclone.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LoginState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {
    
    private val _state = MutableStateFlow(LoginState())
    val state = _state.asStateFlow()
    
    fun login(username: String, password: String) {
        viewModelScope.launch {
            _state.value = LoginState(isLoading = true)
            
            when (val result = authRepository.login(username, password)) {
                is Result.Success -> {
                    _state.value = LoginState(isSuccess = true)
                }
                is Result.Error -> {
                    _state.value = LoginState(error = result.message)
                }
                is Result.Loading -> {}
            }
        }
    }
    
    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }
}
