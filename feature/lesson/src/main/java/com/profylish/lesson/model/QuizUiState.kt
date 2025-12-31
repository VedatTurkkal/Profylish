package com.profylish.lesson.model

import com.profylish.model.lesson.QuizQuestion

sealed interface QuizUiState {
    data object Loading : QuizUiState
    data class Error(val message: String) : QuizUiState

    data class Success(
        val questions: List<QuizQuestion>,

        val currentQuestionIndex: Int = 0,
        val totalQuestions: Int = 0,
        val currentQuestion: QuizQuestion,

        val selectedOptionIndex: Int? = null,
        val isAnswerChecked: Boolean = false,
        val isAnswerCorrect: Boolean = false,

        val score: Int = 0,
        val isLessonCompleted: Boolean = false
    ) : QuizUiState
}