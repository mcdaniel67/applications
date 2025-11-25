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

data class RegisterState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false
)

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {
    
    private val _state = MutableStateFlow(RegisterState())
    val state = _state.asStateFlow()
    
    fun register(username: String, email: String, password: String, displayName: String?) {
        viewModelScope.launch {
            _state.value = RegisterState(isLoading = true)
            
            when (val result = authRepository.register(username, email, password, displayName)) {
                is Result.Success -> {
                    _state.value = RegisterState(isSuccess = true)
                }
                is Result.Error -> {
                    _state.value = RegisterState(error = result.message)
                }
                is Result.Loading -> {}
            }
        }
    }
    
    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }
}
