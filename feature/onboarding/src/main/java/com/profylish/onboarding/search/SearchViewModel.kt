package com.profylish.onboarding.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.profylish.domain.usecase.occupation.SearchOccupationsUseCase
import com.profylish.model.occupation.Occupation
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchOccupationsUseCase: SearchOccupationsUseCase
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadPopularOccupations()

        observeSearchQuery()
    }

    private fun loadPopularOccupations() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val result = searchOccupationsUseCase("")

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        occupations = result
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }

    @OptIn(FlowPreview::class)
    private fun observeSearchQuery() {
        viewModelScope.launch {
            _searchQuery
                .debounce(300L)
                .onEach { query ->
                    _uiState.update { it.copy(isLoading = true) }

                    if (query.isBlank()) {
                        loadPopularOccupations()
                    } else {
                        // Doluysa UseCase üzerinden arama yap
                        try {
                            val searchResults = searchOccupationsUseCase(query)
                            _uiState.update {
                                it.copy(isLoading = false, occupations = searchResults)
                            }
                        } catch (e: Exception) {
                            _uiState.update {
                                it.copy(isLoading = false, errorMessage = "Arama hatası")
                            }
                        }
                    }
                }
                .collect { }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun onOccupationSelected(occupation: Occupation) {
    }
}

data class SearchUiState(
    val occupations: List<Occupation> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)