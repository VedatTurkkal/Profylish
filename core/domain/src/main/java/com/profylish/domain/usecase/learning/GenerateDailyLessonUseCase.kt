package com.profylish.domain.usecase.learning

import com.profylish.domain.repository.AuthRepository
import com.profylish.domain.repository.DictionaryRepository
import com.profylish.model.lesson.QuestionType
import com.profylish.model.lesson.QuizQuestion
import javax.inject.Inject
import kotlin.random.Random

class GenerateDailyLessonUseCase @Inject constructor(
    private val dictionaryRepository: DictionaryRepository,
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(
        profession: String,
        levelId: String, // Roadmap'ten gelen "1", "2", "3" vb.
        category: String // "TERM", "IDIOM", "PHRASAL_VERB", "ACRONYM"
    ): List<QuizQuestion> {

        // Roadmap seviyesini CEFR başlangıç noktasına eşliyoruz
        val baseCefr = mapLevelIdToCefr(levelId)

        // UI kategorisini DB'deki 'type' sütunu yazımına çeviriyoruz
        val dbType = when(category) {
            "TERM" -> "Term"
            "IDIOM" -> "Idiom"
            "PHRASAL_VERB" -> "Phrasal Verb"
            "ACRONYM" -> "Acronym"
            else -> "Term"
        }

        // Kelimeleri çekiyoruz (Repository artık hem Onboarding metnini hem CEFR kodunu anlıyor)
        val words = dictionaryRepository.getWordsForLesson(profession, baseCefr, dbType)
        if (words.isEmpty()) return emptyList()

        val questions = mutableListOf<QuizQuestion>()

        when (dbType) {
            "Term" -> {
                // Term'ler için 4'lü Matching (Eşleştirme) oluştur
                words.chunked(4).forEach { group ->
                    if (group.size >= 2) {
                        questions.add(QuizQuestion(
                            id = group.first().id.toString(),
                            type = QuestionType.MATCHING_PAIRS,
                            questionText = "Match the terms with their definitions",
                            matchingPairs = group.map { it.word to it.definition },
                            explanation = "Terminology is key to professionalism!"
                        ))
                    }
                }
                // Fallback: Yeterli kelime yoksa Çoktan Seçmeli üret
                if (questions.isEmpty()) createMCQuestions(words, questions)
            }

            "Acronym" -> {
                words.forEach { word ->
                    val isCorrect = Random.nextBoolean()
                    val displayedDef = if (isCorrect) word.definition
                    else words.filter { it.id != word.id }.randomOrNull()?.definition ?: "Unknown concept"

                    questions.add(QuizQuestion(
                        id = word.id.toString(),
                        type = QuestionType.TRUE_FALSE,
                        questionText = "Does \"${word.word}\" stand for:\n\n\"$displayedDef\"?",
                        options = listOf("True", "False"),
                        correctAnswerIndex = if (isCorrect) 0 else 1,
                        explanation = "${word.word}: ${word.definition}"
                    ))
                }
            }

            "Idiom", "Phrasal Verb" -> {
                words.forEach { word ->
                    val sentence = word.exampleSentence ?: "${word.word} is commonly used in professional meetings."
                    val blankedSentence = sentence.replace(
                        Regex("\\b${Regex.escape(word.word)}\\b", RegexOption.IGNORE_CASE),
                        "________"
                    )

                    val options = (words.filter { it.id != word.id }.shuffled().take(3).map { it.word } + word.word).shuffled()

                    questions.add(QuizQuestion(
                        id = word.id.toString(),
                        type = QuestionType.FILL_IN_THE_BLANK,
                        questionText = blankedSentence,
                        options = options,
                        correctAnswerIndex = options.indexOf(word.word),
                        targetWord = word.word,
                        explanation = "Example: $sentence"
                    ))
                }
            }
        }
        return questions
    }

    private fun createMCQuestions(words: List<com.profylish.model.curriculum.DictionaryWord>, list: MutableList<QuizQuestion>) {
        words.forEach { w ->
            val opts = (words.filter { it.id != w.id }.shuffled().take(3).map { it.word } + w.word).shuffled()
            list.add(QuizQuestion(
                id = w.id.toString(),
                type = QuestionType.MULTIPLE_CHOICE,
                questionText = "Which term matches this definition?\n\n${w.definition}",
                options = opts,
                correctAnswerIndex = opts.indexOf(w.word)
            ))
        }
    }

    private fun mapLevelIdToCefr(levelId: String): String = when(levelId) {
        "1" -> "B1" // Starting from Scratch -> B1-B2 havuzu
        "2" -> "B2" // Some Knowledge -> B2-C1 havuzu
        "3" -> "C1" // Experienced -> C1-C2 havuzu
        else -> "B1"
    }
}