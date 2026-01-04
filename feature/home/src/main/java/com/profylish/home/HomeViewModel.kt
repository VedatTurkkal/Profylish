package com.profylish.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.profylish.common.util.ConnectivityObserver
import com.profylish.common.util.ConnectivityStatus
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
    private val curriculumRepository: CurriculumRepository,
    private val connectivityObserver: ConnectivityObserver
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState(isLoading = true)) // Başlangıçta loading olsun
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        observeUserData()
        observeConnectivity()
    }

    private fun observeUserData() {
        viewModelScope.launch {
            // Burada userData'yı dinliyoruz. Loglara göre veri geliyor.
            userDataRepository.userData.collectLatest { userPrefs ->
                // Eğer veri geldiyse ve activeCourseId varsa işleriz
                if (userPrefs.activeCourseId != null) {
                    handleUserPrefs(userPrefs)
                } else {
                    // Kullanıcı yeni gelmiş olabilir, loading'i kapat ama veriyi boş göster
                    _uiState.update { it.copy(isLoading = false) }
                }
            }
        }
    }

    private fun observeConnectivity() {
        viewModelScope.launch {
            connectivityObserver.observe().collectLatest { status ->
                if (status == ConnectivityStatus.Available) {
                    try {
                        userDataRepository.restoreFromCloud()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    private suspend fun handleUserPrefs(userPrefs: UserPreferences) {
        val activeJobTitle = userPrefs.activeCourseId ?: return

        // Artık loading'i burada tekrar true yapmıyoruz ki ekran titremesin,
        // arka planda güncelleyelim.

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
                nodes = realNodes,
                isVibrationEnabled = userPrefs.isVibrationEnabled,
                completedCategoriesByLevel = userPrefs.completedCategories
            )
        }
    }

    fun switchCourse(courseName: String) {
        viewModelScope.launch {
            userDataRepository.switchOrAddCourse(courseName)
        }
    }
}