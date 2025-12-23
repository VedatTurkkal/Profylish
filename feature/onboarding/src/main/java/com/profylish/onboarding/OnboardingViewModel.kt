package com.profylish.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.profylish.domain.repository.UserDataRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val userDataRepository: UserDataRepository
) : ViewModel() {

    // Parametre güncellendi
    fun saveUserPreference(occupation: String, group: String, level: String) {
        viewModelScope.launch {
            userDataRepository.saveUserSelection(occupation, group, level)
        }
    }
}