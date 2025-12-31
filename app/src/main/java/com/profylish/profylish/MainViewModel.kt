package com.profylish.profylish

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.profylish.domain.repository.UserDataRepository
import com.profylish.model.user.UserPreferences
import com.profylish.profylish.navigation.TopLevelDestination
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    userDataRepository: UserDataRepository
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

    // TEMA KONTROLÜ
    val isDarkMode: StateFlow<Boolean> = userDataRepository.userData
        .map { it.isDarkModeEnabled }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
}