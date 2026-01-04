package com.profylish.onboarding

import android.content.Context
import androidx.core.content.edit // KTX eklentisi için gerekli import
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.profylish.domain.repository.UserDataRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val userDataRepository: UserDataRepository,
    @param:ApplicationContext private val context: Context
) : ViewModel() {

    fun completeOnboarding(jobTitle: String) {
        viewModelScope.launch {
            userDataRepository.switchOrAddCourse(jobTitle)

            val sharedPref = context.getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)

            sharedPref.edit {
                putBoolean("onboarding_complete", true)
            }
        }
    }
}