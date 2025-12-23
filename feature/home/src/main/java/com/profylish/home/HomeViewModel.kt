package com.profylish.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.profylish.domain.repository.CurriculumRepository
import com.profylish.domain.repository.UserDataRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
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
        loadRoadmap()
    }

    private fun loadRoadmap() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                // 1. Kullanıcı tercihlerini (Tüm objeyi) çek
                val userPrefs = userDataRepository.userData.first()

                // ✅ ARTIK GRUP İSMİNİ KULLANIYORUZ
                val groupToSearch = userPrefs.occupationGroup

                // Eğer grup yoksa (eski veri vs.) fallback olarak ID'yi (meslek adını) kullan
                val searchTerm = if (!groupToSearch.isNullOrBlank()) groupToSearch else userPrefs.occupationId

                if (searchTerm.isNullOrBlank()) {
                    _uiState.update { it.copy(isLoading = false) }
                    return@launch
                }

                // 2. Repository'e arama terimini (Grup veya Ad) gönder
                val realNodes = curriculumRepository.generateRoadmap(searchTerm)

                _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        level = 1,
                        gems = 150,
                        hearts = 5,
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