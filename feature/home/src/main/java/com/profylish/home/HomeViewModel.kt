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
            userDataRepository.userData.collectLatest { userPrefs: UserPreferences ->
                _uiState.update { it.copy(isLoading = true) }

                try {
                    val activeJobTitle = userPrefs.activeCourseId

                    if (activeJobTitle.isNullOrBlank()) {
                        _uiState.update { it.copy(isLoading = false) }
                        return@collectLatest
                    }

                    val progress = userPrefs.courses[activeJobTitle] ?: CourseProgress()

                    val realNodes = curriculumRepository.generateRoadmap(
                        occupationTitle = activeJobTitle,
                        currentLevel = progress.level
                    )

                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            profession = activeJobTitle,
                            availableCourses = userPrefs.courses.keys.toList(), // Kurs listesini çek
                            level = progress.level,
                            gems = userPrefs.gems,
                            hearts = userPrefs.hearts,
                            nodes = realNodes
                        )
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    _uiState.update { it.copy(isLoading = false) }
                }
            }
        }
    }

    // Kurs değiştirme fonksiyonu
    fun switchCourse(courseName: String) {
        viewModelScope.launch {
            userDataRepository.switchOrAddCourse(courseName)
        }
    }
}