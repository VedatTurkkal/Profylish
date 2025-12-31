package com.profylish.lesson.quiz

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.profylish.domain.repository.AuthRepository
import com.profylish.domain.repository.DictionaryRepository
import com.profylish.domain.repository.UserDataRepository
import com.profylish.domain.usecase.learning.GenerateDailyLessonUseCase
import com.profylish.lesson.model.QuizUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface QuizNavigationEvent {
    data object NavigateToAuth : QuizNavigationEvent
    data object NavigateHome : QuizNavigationEvent
    data object VibrateSuccess : QuizNavigationEvent
    data object VibrateError : QuizNavigationEvent
}

@HiltViewModel
class QuizViewModel @Inject constructor(
    private val generateDailyLessonUseCase: GenerateDailyLessonUseCase,
    private val userDataRepository: UserDataRepository,
    private val dictionaryRepository: DictionaryRepository,
    private val authRepository: AuthRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val levelId: String = savedStateHandle.get<String>("levelId") ?: "1"
    private val profession: String = savedStateHandle.get<String>("profession") ?: "Software Engineer"

    // YENİ: Bu ders ilerleme dersi mi? (Navigasyondan gelir)
    private val isProgression: Boolean = savedStateHandle.get<Boolean>("isProgression") ?: true

    private val _uiState = MutableStateFlow<QuizUiState>(QuizUiState.Loading)
    val uiState: StateFlow<QuizUiState> = _uiState.asStateFlow()

    private val _navigationEvent = Channel<QuizNavigationEvent>()
    val navigationEvent = _navigationEvent.receiveAsFlow()

    init {
        loadLesson()
    }

    private fun loadLesson() {
        viewModelScope.launch {
            _uiState.value = QuizUiState.Loading
            try {
                val questions = generateDailyLessonUseCase(profession, levelId)
                if (questions.isNotEmpty()) {
                    _uiState.value = QuizUiState.Success(
                        questions = questions,
                        currentQuestionIndex = 0,
                        totalQuestions = questions.size,
                        currentQuestion = questions[0],
                        score = 0
                    )
                } else {
                    _uiState.value = QuizUiState.Error("No content found for $profession.")
                }
            } catch (e: Exception) {
                _uiState.value = QuizUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun onOptionSelected(index: Int) {
        val currentState = _uiState.value
        if (currentState is QuizUiState.Success && !currentState.isAnswerChecked) {
            _uiState.value = currentState.copy(selectedOptionIndex = index)
        }
    }

    fun onCheckAnswer() {
        val currentState = _uiState.value
        if (currentState is QuizUiState.Success && currentState.selectedOptionIndex != null) {
            val isCorrect = currentState.selectedOptionIndex == currentState.currentQuestion.correctAnswerIndex

            viewModelScope.launch {
                if (isCorrect) {
                    _navigationEvent.send(QuizNavigationEvent.VibrateSuccess)
                } else {
                    _navigationEvent.send(QuizNavigationEvent.VibrateError)
                    userDataRepository.deductHeart()
                }
            }

            _uiState.value = currentState.copy(
                isAnswerChecked = true,
                isAnswerCorrect = isCorrect,
                score = if (isCorrect) currentState.score + 10 else currentState.score
            )
        }
    }

    fun onNextQuestion() {
        val currentState = _uiState.value
        if (currentState is QuizUiState.Success) {
            val nextIndex = currentState.currentQuestionIndex + 1
            if (nextIndex < currentState.questions.size) {
                _uiState.value = currentState.copy(
                    currentQuestionIndex = nextIndex,
                    currentQuestion = currentState.questions[nextIndex],
                    selectedOptionIndex = null,
                    isAnswerChecked = false,
                    isAnswerCorrect = false
                )
            } else {
                _uiState.value = currentState.copy(isLessonCompleted = true)
            }
        }
    }

    fun onLessonFinished() {
        val currentState = _uiState.value
        if (currentState !is QuizUiState.Success) return

        val totalPossible = currentState.totalQuestions * 10
        val percentage = if (totalPossible > 0) (currentState.score.toFloat() / totalPossible) * 100 else 0f

        viewModelScope.launch {
            if (percentage >= 70) {
                // 1. XP ve Elmas her zaman verilir
                userDataRepository.updateUserStats(xpEarned = currentState.score, gemsEarned = 15)

                // 2. KRİTİK KONTROL: Sadece "Progression" dersiyse Level/Stage artır
                if (isProgression) {
                    userDataRepository.unlockNextLevel()
                }

                // 3. Öğrenilen kelimeleri kaydet
                val userId = authRepository.getCurrentUserId()
                if (userId != null) {
                    val wordIdsUsedInLesson = currentState.questions.mapNotNull {
                        it.id.toString().toIntOrNull()
                    }
                    dictionaryRepository.markWordsAsLearned(userId, wordIdsUsedInLesson)
                }

                if (!authRepository.isUserLoggedIn()) {
                    _navigationEvent.send(QuizNavigationEvent.NavigateToAuth)
                } else {
                    _navigationEvent.send(QuizNavigationEvent.NavigateHome)
                }
            } else {
                _navigationEvent.send(QuizNavigationEvent.NavigateHome)
            }
        }
    }
}