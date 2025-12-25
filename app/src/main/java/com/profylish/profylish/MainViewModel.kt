package com.profylish.profylish

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.profylish.model.user.LearnerProfile
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor() : ViewModel() {

    private val _userProfile = MutableStateFlow(LearnerProfile())
    val userProfile: StateFlow<LearnerProfile> = _userProfile.asStateFlow()

    init {
        viewModelScope.launch {
            delay(1000)

            _userProfile.value = LearnerProfile(
                currentProfession = "UX Designer",
                gems = 50,
                streak = 1,
                hearts = 5
            )
        }
    }

    fun updateProfession(newProfession: String) {
        val current = _userProfile.value
        _userProfile.value = current.copy(currentProfession = newProfession)
    }
}