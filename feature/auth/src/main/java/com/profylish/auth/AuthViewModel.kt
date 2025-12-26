package com.profylish.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.profylish.domain.repository.AuthRepository
import com.profylish.domain.repository.UserDataRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
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

    fun toggleMode() {
        _uiState.update { it.copy(isLoginMode = !it.isLoginMode) }
    }

    fun submit() {
        val state = _uiState.value
        if (state.email.isBlank() || state.password.isBlank()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val result = if (state.isLoginMode) {
                authRepository.signIn(state.email, state.password)
            } else {
                authRepository.signUp(state.email, state.password)
            }

            result.onSuccess {
                // Kayıt/Giriş sonrası veriyi restore et (Otomatik giriş hissi verir)
                userDataRepository.restoreFromCloud()

                _uiState.update { it.copy(isLoading = false) }
                _authEvent.send(AuthEvent.AuthSuccess)
            }.onFailure { error ->
                _uiState.update { it.copy(isLoading = false) }
                _authEvent.send(AuthEvent.AuthError(error.message ?: "Authentication failed"))
            }
        }
    }
}