package com.profylish.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.profylish.domain.repository.AuthRepository
import com.profylish.domain.repository.UserDataRepository
import com.profylish.model.user.UserPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userDataRepository: UserDataRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    // Sadece ayarlarla ilgileniyoruz, tüm profili çekmeye gerek yok ama
    // UserPreferences içinde ayarlar olduğu için onu dinliyoruz.
    val userPreferences: StateFlow<UserPreferences> = userDataRepository.userData
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = UserPreferences()
        )

    val isLoggedIn = authRepository.isUserLoggedIn()

    fun updateSettings(vibration: Boolean, darkMode: Boolean, notifications: Boolean) {
        viewModelScope.launch {
            userDataRepository.updateSettings(vibration, darkMode, notifications)
        }
    }

    fun signOut() {
        viewModelScope.launch {
            authRepository.signOut()
            userDataRepository.clearLocalData()
        }
    }
}