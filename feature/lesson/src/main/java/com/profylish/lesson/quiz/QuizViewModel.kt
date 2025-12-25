package com.profylish.lesson.quiz

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.profylish.domain.usecase.learning.GenerateDailyLessonUseCase
import com.profylish.lesson.model.QuizUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class QuizViewModel @Inject constructor(
    private val generateDailyLessonUseCase: GenerateDailyLessonUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val levelId: String = savedStateHandle.get<String>("levelId") ?: "1"
    private val profession: String = savedStateHandle.get<String>("profession") ?: "Software Engineer"

    private val _uiState = MutableStateFlow<QuizUiState>(QuizUiState.Loading)
    val uiState: StateFlow<QuizUiState> = _uiState.asStateFlow()

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
}