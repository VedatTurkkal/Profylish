package com.profylish.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.profylish.domain.repository.CurriculumRepository
import com.profylish.domain.repository.UserDataRepository
import com.profylish.model.user.CourseProgress
import com.profylish.model.user.UserPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val userDataRepository: UserDataRepository,
    private val curriculumRepository: CurriculumRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        observeUserData()
    }

    private fun observeUserData() {
        viewModelScope.launch {
            userDataRepository.userData.collectLatest { userPrefs ->
                handleUserPrefs(userPrefs)
            }
        }
    }

    private suspend fun handleUserPrefs(userPrefs: UserPreferences) {
        val activeJobTitle = userPrefs.activeCourseId ?: return

        _uiState.update { it.copy(isLoading = true) }

        val progress = userPrefs.courses[activeJobTitle] ?: CourseProgress()

        val realNodes = curriculumRepository.generateRoadmap(
            occupationTitle = activeJobTitle,
            currentLevel = progress.level
        )

        _uiState.update {
            it.copy(
                isLoading = false,
                profession = activeJobTitle,
                availableCourses = userPrefs.courses.keys.toList(),

                level = progress.level,
                currentStage = progress.stagesCompleted,

                gems = userPrefs.gems,
                hearts = userPrefs.hearts,
                streak = userPrefs.streak,
                nodes = realNodes
            )
        }
    }

    fun switchCourse(courseName: String) {
        viewModelScope.launch {
            userDataRepository.switchOrAddCourse(courseName)
        }
    }
}