package com.example.googleauthlab.ui.features.login

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.googleauthlab.data.remote.auth.AuthRes
import com.example.googleauthlab.data.remote.auth.GoogleAuthClient
import com.example.googleauthlab.domain.model.UserProfile
import com.example.googleauthlab.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val googleAuthClient: GoogleAuthClient
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private val _events = Channel<LoginUiEvent>(Channel.BUFFERED)
    val event = _events.receiveAsFlow()

    init {
        observeCurrentUser()
    }

    fun signInWithGoogle(context: Context) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true, errorMsg = null
                )
            }

            val res = when (val tokenRes = googleAuthClient.getGoogleIdToken(context)) {
                is AuthRes.Success -> {
                    authRepository.signInWithGoogle(tokenRes.data)
                }

                is AuthRes.Error -> {
                    AuthRes.Error(tokenRes.errorMsg)
                }
            }

            handleAuthResult(res)
        }
    }

    fun signInAnonymously() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true, errorMsg = null
                )
            }

            val res = authRepository.createAnonymouslyAccount()

            handleAuthResult(res)
        }
    }

    private fun observeCurrentUser() {
        viewModelScope.launch {
            authRepository.currentUser.collect { user ->
                _uiState.update {
                    it.copy(
                        user = user,
                        isCheckingSession = false
                    )
                }
            }
        }
    }

    fun sighOut(context: Context) {
        viewModelScope.launch {
            authRepository.signOut()
            googleAuthClient.clearCredentialState(context)
        }
    }

    private suspend fun handleAuthResult(result: AuthRes<UserProfile>) {
        when (result) {
            is AuthRes.Success -> {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        user = result.data,
                        errorMsg = null
                    )
                }

                _events.send(LoginUiEvent.NavigateToHome)
            }

            is AuthRes.Error -> {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMsg = result.errorMsg
                    )
                }

                _events.send(
                    LoginUiEvent.ShowError(result.errorMsg)
                )
            }
        }
    }

}