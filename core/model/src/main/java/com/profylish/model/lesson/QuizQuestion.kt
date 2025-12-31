package com.profylish.model.lesson

enum class QuestionType {
    MULTIPLE_CHOICE,
    FILL_IN_THE_BLANK,
    MATCHING_PAIRS,
    TRUE_FALSE
}

data class QuizQuestion(
    val id: String,
    val type: QuestionType,
    val questionText: String,
    val options: List<String>,
    val correctAnswerIndex: Int,
    val explanation: String? = null,
    val targetWord: String? = null
)