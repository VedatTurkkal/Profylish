package com.profylish.leaderboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.profylish.domain.usecase.leaderboard.GetCurrentLeagueUseCase
import com.profylish.model.leaderboard.LeaderboardEntry
import com.profylish.model.leaderboard.LeagueTier
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.Duration
import java.time.LocalDateTime
import java.time.temporal.TemporalAdjusters
import javax.inject.Inject

sealed interface LeaderboardUiState {
    object Loading : LeaderboardUiState
    data class Success(
        val tier: LeagueTier,
        val entries: List<LeaderboardEntry>,
        val currentUserRank: Int,
        val timeRemaining: String
    ) : LeaderboardUiState
    data class Error(val message: String) : LeaderboardUiState
}

@HiltViewModel
class LeaderboardViewModel @Inject constructor(
    private val getCurrentLeagueUseCase: GetCurrentLeagueUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<LeaderboardUiState>(LeaderboardUiState.Loading)
    val uiState: StateFlow<LeaderboardUiState> = _uiState.asStateFlow()

    init {
        // İlk açılışta veriyi çek (Loading göster)
        fetchLeaderboardData(isInitialLoad = true)
        // Geri sayımı başlat
        startTimerLoop()
    }

    // isInitialLoad parametresi eklendi
    fun fetchLeaderboardData(isInitialLoad: Boolean = false) {
        viewModelScope.launch {
            // Eğer bu ilk yükleme DEĞİLSE ve zaten veri varsa, Loading gösterme (Sessiz Yenileme)
            if (isInitialLoad || _uiState.value !is LeaderboardUiState.Success) {
                _uiState.value = LeaderboardUiState.Loading
            }

            getCurrentLeagueUseCase()
                .catch { e ->
                    // Hata durumunda UI'ı bozmamak için eski veri varsa korunabilir ama
                    // şimdilik basit hata mesajı gösteriyoruz.
                    _uiState.value = LeaderboardUiState.Error("Error: ${e.localizedMessage}")
                }
                .collect { (tier, entries) ->
                    val myRank = entries.find { it.isCurrentUser }?.rank ?: 0

                    _uiState.value = LeaderboardUiState.Success(
                        tier = tier,
                        entries = entries,
                        currentUserRank = myRank,
                        timeRemaining = calculateTimeRemaining()
                    )
                }
        }
    }

    private fun startTimerLoop() {
        viewModelScope.launch {
            while (isActive) {
                val currentState = _uiState.value
                if (currentState is LeaderboardUiState.Success) {
                    _uiState.value = currentState.copy(timeRemaining = calculateTimeRemaining())
                }
                delay(60_000) // 1 Dakikada bir güncelle
            }
        }
    }

    private fun calculateTimeRemaining(): String {
        return try {
            val now = LocalDateTime.now()
            val nextMonday = now.with(TemporalAdjusters.next(DayOfWeek.MONDAY))
                .withHour(0).withMinute(0).withSecond(0)

            val duration = Duration.between(now, nextMonday)
            val days = duration.toDays()
            val hours = duration.toHours() % 24

            if (days > 0) "$days Days $hours Hours Left" else "$hours Hours Left"
        } catch (e: Exception) {
            "Ending soon"
        }
    }
}