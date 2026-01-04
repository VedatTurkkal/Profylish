package com.profylish.profylish

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.profylish.common.util.ConnectivityObserver
import com.profylish.common.util.ConnectivityStatus
import com.profylish.domain.repository.AuthRepository
import com.profylish.domain.repository.UserDataRepository
import com.profylish.model.user.UserPreferences
import com.profylish.profylish.navigation.TopLevelDestination
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val userDataRepository: UserDataRepository,
    private val connectivityObserver: ConnectivityObserver,
    private val authRepository: AuthRepository // Eklendi
) : ViewModel() {

    // Splash ekranı için Loading State
    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    val startDestination: StateFlow<String?> = userDataRepository.userData
        .map { prefs: UserPreferences ->
            if (!prefs.activeCourseId.isNullOrBlank()) {
                TopLevelDestination.HOME.route
            } else {
                "onboarding_flow"
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val isDarkMode: StateFlow<Boolean> = userDataRepository.userData
        .map { it.isDarkModeEnabled }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val isOffline: StateFlow<Boolean> = connectivityObserver.observe()
        .map { it != ConnectivityStatus.Available }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    init {
        checkSessionAndRestore()
        observeConnectivity()
    }

    // Uygulama açılışında Session kontrolü ve Veri Restorasyonu
    private fun checkSessionAndRestore() {
        viewModelScope.launch {
            _isLoading.value = true

            // Eğer Supabase'de oturum açıksa (token varsa)
            if (authRepository.isUserLoggedIn()) {
                try {
                    // Buluttan en güncel veriyi çek ve Local DataStore'a yaz
                    userDataRepository.restoreFromCloud()
                } catch (e: Exception) {
                    e.printStackTrace()
                    // Hata olsa bile devam et, var olan local veriyle açılacak
                }
            }
            // İşlem bitti, splash kalkabilir
            _isLoading.value = false
        }
    }

    private fun observeConnectivity() {
        viewModelScope.launch {
            connectivityObserver.observe().collectLatest { status ->
                if (status == ConnectivityStatus.Available) {
                    // İnternet geri geldiğinde, Local veriyi Cloud'a senkronize et.
                    if (authRepository.isUserLoggedIn()) {
                        userDataRepository.syncLocalDataToCloud()
                    }
                }
            }
        }
    }
}