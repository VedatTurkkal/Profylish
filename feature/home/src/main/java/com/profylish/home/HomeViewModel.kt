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
                // 1. Kullanıcı tercihlerini DataStore'dan çek
                val userPrefs = userDataRepository.userData.first()

                val groupToSearch = userPrefs.occupationGroup
                // Eğer grup varsa onu, yoksa occupationId'yi (meslek adını) kullan
                val searchTerm = if (!groupToSearch.isNullOrBlank()) groupToSearch else userPrefs.occupationId

                if (searchTerm.isNullOrBlank()) {
                    _uiState.update { it.copy(isLoading = false) }
                    return@launch
                }

                // 2. Yol haritasını oluştur
                val realNodes = curriculumRepository.generateRoadmap(searchTerm)

                // 3. UI State'i güncelle
                _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        profession = searchTerm, // <-- KRİTİK: Mesleği buraya kaydediyoruz
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