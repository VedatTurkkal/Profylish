package com.profylish.model.lesson

enum class QuestionType {
    MULTIPLE_CHOICE,
    FILL_IN_THE_BLANK,
    TRUE_FALSE,
    MATCHING_PAIRS,
}

data class QuizQuestion(
    val id: String,
    val type: QuestionType,
    val questionText: String,
    val options: List<String> = emptyList(),
    val correctAnswerIndex: Int = -1,
    val explanation: String? = null,
    val targetWord: String? = null,
    val matchingPairs: List<Pair<String, String>> = emptyList(),
)