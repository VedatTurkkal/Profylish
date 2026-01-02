package com.profylish.profylish

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.profylish.common.util.ConnectivityObserver
import com.profylish.common.util.ConnectivityStatus
import com.profylish.domain.repository.UserDataRepository
import com.profylish.model.user.UserPreferences
import com.profylish.profylish.navigation.TopLevelDestination
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val userDataRepository: UserDataRepository,
    private val connectivityObserver: ConnectivityObserver
) : ViewModel() {

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

    // --- EKLENEN KISIM: İNTERNET GELDİĞİNDE SENKRONİZASYON ---
    init {
        observeConnectivity()
    }

    private fun observeConnectivity() {
        viewModelScope.launch {
            connectivityObserver.observe().collectLatest { status ->
                if (status == ConnectivityStatus.Available) {
                    // İnternet geri geldiğinde, Local veriyi (örn: 2 kalp) Cloud'a YAZ.
                    // Asla Cloud'dan otomatik çekme (restore yapma), çünkü Cloud verisi eskidir.
                    userDataRepository.syncLocalDataToCloud()
                }
            }
        }
    }
}