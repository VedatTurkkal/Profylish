package com.profylish.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.profylish.domain.repository.AuthRepository
import com.profylish.domain.repository.UserDataRepository
import com.profylish.model.user.UserPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileUiState(
    val userPreferences: UserPreferences = UserPreferences(),
    val isLoggedIn: Boolean = false
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    userDataRepository: UserDataRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    val uiState: StateFlow<ProfileUiState> = combine(
        userDataRepository.userData,
        authRepository.authState
    ) { preferences, isLoggedIn ->
        ProfileUiState(
            userPreferences = preferences,
            isLoggedIn = isLoggedIn
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ProfileUiState(isLoggedIn = authRepository.isUserLoggedIn())
    )

    fun signOut() {
        viewModelScope.launch {
            authRepository.signOut()
        }
    }
}