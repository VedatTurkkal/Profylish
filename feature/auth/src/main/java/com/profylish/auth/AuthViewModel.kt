package com.profylish.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.profylish.domain.repository.AuthRepository
import com.profylish.domain.repository.UserDataRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface AuthEvent {
    data object AuthSuccess : AuthEvent
    data class AuthError(val message: String) : AuthEvent
}

data class AuthUiState(
    val isLoading: Boolean = false,
    val email: String = "",
    val password: String = "",
    val username: String = "",
    val isLoginMode: Boolean = true
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userDataRepository: UserDataRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState = _uiState.asStateFlow()

    private val _authEvent = Channel<AuthEvent>()
    val authEvent = _authEvent.receiveAsFlow()

    fun onEmailChange(newValue: String) {
        _uiState.update { it.copy(email = newValue) }
    }

    fun onPasswordChange(newValue: String) {
        _uiState.update { it.copy(password = newValue) }
    }

    fun onUsernameChange(newValue: String) {
        _uiState.update { it.copy(username = newValue) }
    }

    fun toggleMode() {
        _uiState.update { it.copy(isLoginMode = !it.isLoginMode) }
    }

    fun authenticate() {
        val state = _uiState.value

        if (state.email.isBlank() || state.password.isBlank()) return
        if (!state.isLoginMode && state.username.isBlank()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            if (state.isLoginMode) {
                performLogin(state.email, state.password)
            } else {
                performSignUp(state.email, state.password, state.username)
            }

            _uiState.update { it.copy(isLoading = false) }
        }
    }

    private suspend fun performLogin(email: String, pass: String) {
        authRepository.signIn(email, pass)
            .onSuccess {
                try {
                    userDataRepository.restoreFromCloud()
                    _authEvent.send(AuthEvent.AuthSuccess)
                } catch (_: Exception) {
                    _authEvent.send(AuthEvent.AuthSuccess)
                }
            }
            .onFailure {
                _authEvent.send(AuthEvent.AuthError(it.message ?: "Login failed"))
            }
    }

    private suspend fun performSignUp(email: String, pass: String, username: String) {
        authRepository.signUp(email, pass, username)
            .onSuccess {
                try {
                    delay(500)

                    userDataRepository.syncLocalDataToCloud()

                    userDataRepository.updateUsername(username)

                    _authEvent.send(AuthEvent.AuthSuccess)
                } catch (_: Exception) {
                    _authEvent.send(AuthEvent.AuthSuccess)
                }
            }
            .onFailure {
                _authEvent.send(AuthEvent.AuthError(it.message ?: "Sign up failed"))
            }
    }
}