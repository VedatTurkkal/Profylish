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

    // XP Sorunu Çözümü 1: SharingStarted.Eagerly yaparak ekran açılır açılmaz veriyi dinlemeye zorluyoruz.
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
        started = SharingStarted.Eagerly,
        initialValue = ProfileUiState(isLoggedIn = authRepository.isUserLoggedIn())
    )

    init {
        checkAndSyncProfile()
    }

    private fun checkAndSyncProfile() {
        viewModelScope.launch {
            if (authRepository.isUserLoggedIn()) {
                try {
                    // XP Sorunu Çözümü 2: Buluttan veriyi zorla çekiyoruz.
                    userDataRepository.syncLocalDataToCloud()
                    userDataRepository.restoreFromCloud()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun updateUsername(newName: String) {
        viewModelScope.launch {
            userDataRepository.updateUsername(newName)
        }
    }

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

    // Bu fonksiyon Settings ekranından çağırılacak, o yüzden uyarıyı dikkate alma.
    fun signOut() {
        viewModelScope.launch {
            authRepository.signOut()
            userDataRepository.clearLocalData()
        }
    }

    // Bu fonksiyon Settings ekranından çağırılacak.
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