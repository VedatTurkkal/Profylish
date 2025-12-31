package com.profylish.profile

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.profylish.domain.repository.AuthRepository
import com.profylish.domain.repository.UserDataRepository
import com.profylish.model.user.UserPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

data class ProfileUiState(
    val userPreferences: UserPreferences = UserPreferences(),
    val isLoggedIn: Boolean = false
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userDataRepository: UserDataRepository,
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

    init {
        checkAndSyncProfile()
    }

    private fun checkAndSyncProfile() {
        viewModelScope.launch {
            if (authRepository.isUserLoggedIn()) {
                try {
                    userDataRepository.restoreFromCloud()
                } catch (e: Exception) {
                    // Sessizce başarısız olabilir veya loglanabilir
                }
            }
        }
    }

    fun updateUsername(newName: String) {
        viewModelScope.launch {
            try {
                userDataRepository.updateUsername(newName)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // YENİ: Galeriden seçilen resmi işler
    fun onAvatarSelected(context: Context, uri: Uri) {
        viewModelScope.launch {
            try {
                val imageBytes = withContext(Dispatchers.IO) {
                    context.contentResolver.openInputStream(uri)?.use {
                        it.readBytes()
                    }
                }

                if (imageBytes != null) {
                    userDataRepository.uploadAvatar(imageBytes)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            authRepository.signOut()
            userDataRepository.clearLocalData()
        }
    }

    fun updateSettings(vibration: Boolean, darkMode: Boolean, notifications: Boolean) {
        viewModelScope.launch {
            userDataRepository.updateSettings(vibration, darkMode, notifications)
        }
    }

    fun deleteAvatar() {
        viewModelScope.launch {
            userDataRepository.deleteAvatar()
        }
    }
}