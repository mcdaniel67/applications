package com.example.twitterclone.presentation.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.twitterclone.domain.model.Tweet
import com.example.twitterclone.domain.repository.AuthRepository
import com.example.twitterclone.domain.repository.TweetRepository
import com.example.twitterclone.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FeedState(
    val tweets: List<Tweet> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isRefreshing: Boolean = false,
    val isGlobalFeed: Boolean = true
)

@HiltViewModel
class FeedViewModel @Inject constructor(
    private val tweetRepository: TweetRepository,
    private val authRepository: AuthRepository
) : ViewModel() {
    
    private val _state = MutableStateFlow(FeedState())
    val state = _state.asStateFlow()
    
    init {
        loadFeed()
    }
    
    fun loadFeed() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            
            val result = if (_state.value.isGlobalFeed) {
                tweetRepository.getGlobalFeed(1, 20)
            } else {
                tweetRepository.getFeed(1, 20)
            }
            
            when (result) {
                is Result.Success -> {
                    _state.value = _state.value.copy(
                        tweets = result.data,
                        isLoading = false
                    )
                }
                is Result.Error -> {
                    _state.value = _state.value.copy(
                        error = result.message,
                        isLoading = false
                    )
                }
                is Result.Loading -> {}
            }
        }
    }
    
    fun switchFeed(isGlobal: Boolean) {
        _state.value = _state.value.copy(isGlobalFeed = isGlobal)
        loadFeed()
    }
    
    fun refresh() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isRefreshing = true, error = null)
            
            when (val result = tweetRepository.getFeed(1, 20)) {
                is Result.Success -> {
                    _state.value = FeedState(tweets = result.data)
                }
                is Result.Error -> {
                    _state.value = _state.value.copy(
                        isRefreshing = false,
                        error = result.message
                    )
                }
                is Result.Loading -> {}
            }
        }
    }
    
    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
        }
    }
}
