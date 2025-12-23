package com.profylish.onboarding.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.profylish.domain.usecase.occupation.SearchOccupationsUseCase
import com.profylish.model.occupation.Occupation
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel // ✅ EKLENDİ
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow // ✅ EKLENDİ
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

    // 👇 EKSİK OLAN KISIM BURASIYDI 👇
    private val _navigationEvent = Channel<SearchNavigationEvent>()
    val navigationEvent = _navigationEvent.receiveAsFlow()
    // 👆 ---------------------------- 👆

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
                        try {
                            val searchResults = searchOccupationsUseCase(query)
                            _uiState.update {
                                it.copy(isLoading = false, occupations = searchResults)
                            }
                        } catch (e: Exception) {
                            _uiState.update {
                                it.copy(isLoading = false, errorMessage = e.message)
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
        viewModelScope.launch {
            val groupName = occupation.codes?.onetGroup ?: occupation.title

            // Artık _navigationEvent tanımlı olduğu için bu satır hata vermeyecek
            _navigationEvent.send(
                SearchNavigationEvent.NavigateToPersonalization(
                    occupationId = occupation.title,
                    occupationGroup = groupName
                )
            )
        }
    }
}

// Sealed Interface ve Data Class (Aynı kalıyor)
sealed interface SearchNavigationEvent {
    data class NavigateToPersonalization(
        val occupationId: String,
        val occupationGroup: String
    ) : SearchNavigationEvent
}

data class SearchUiState(
    val occupations: List<Occupation> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)