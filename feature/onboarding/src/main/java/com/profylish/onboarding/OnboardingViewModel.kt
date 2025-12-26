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

    fun saveUserPreference(jobTitle: String) {
        viewModelScope.launch {
            userDataRepository.switchOrAddCourse(jobTitle)
        }
    }
}