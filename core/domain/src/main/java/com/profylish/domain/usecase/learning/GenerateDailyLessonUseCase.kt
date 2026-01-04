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
        levelId: String,
        category: String
    ): List<QuizQuestion> {

        val baseCefr = mapLevelIdToCefr(levelId)

        // Kategori ismini veritabanı tipine çevir
        val dbType = when(category) {
            "TERM" -> "Term"
            "IDIOM" -> "Idiom"
            "PHRASAL_VERB" -> "Phrasal Verb"
            "ACRONYM" -> "Acronym"
            else -> "Term"
        }

        // 1. Havuzdan kelimeleri çek (Yeterli sayıda çekmeye çalışıyoruz)
        val allWords = dictionaryRepository.getWordsForLesson(profession, baseCefr, dbType)

        if (allWords.isEmpty()) return emptyList()

        val questions = mutableListOf<QuizQuestion>()

        when (dbType) {
            // --- TERMINOLOGY: Eşleştirme (Matching) ---
            "Term" -> {
                // Eşleştirme için en az 4 kelime lazım (2 soru x 2 çift).
                // İdeal olan 10 kelime (5 soru x 2 çift).
                if (allWords.size >= 4) {
                    val pool = allWords.shuffled()
                    // 2'şerli gruplara ayır. Her grup bir "Eşleştirme Sorusu" olur.
                    // Örneğin 10 kelime -> 5 grup -> 5 Soru.
                    pool.chunked(2).forEach { group ->
                        if (group.size == 2) {
                            questions.add(QuizQuestion(
                                id = group.first().id.toString(), // ID temsili
                                type = QuestionType.MATCHING_PAIRS,
                                questionText = "Match the terms with their definitions",
                                // List<Pair<String, String>>
                                matchingPairs = group.map { it.word to it.definition },
                                explanation = "Terminology is key!"
                            ))
                        }
                    }
                }
                // Çok az kelime varsa mecburen Çoktan Seçmeli (MC)
                else {
                    val targetWords = allWords.shuffled().take(5)
                    targetWords.forEach { targetWord ->
                        val options = (allWords.filter { it.id != targetWord.id }.shuffled().take(3).map { it.word } + targetWord.word)
                            .map { it.trim() }.distinct().shuffled()

                        val correctIndex = options.indexOf(targetWord.word.trim())
                        if (correctIndex != -1) {
                            questions.add(QuizQuestion(
                                id = targetWord.id.toString(),
                                type = QuestionType.MULTIPLE_CHOICE,
                                questionText = "Which term matches this definition?\n\n\"${targetWord.definition}\"",
                                options = options,
                                correctAnswerIndex = correctIndex,
                                explanation = "${targetWord.word}: ${targetWord.definition}"
                            ))
                        }
                    }
                }
            }

            // --- ACRONYM: Doğru/Yanlış ---
            "Acronym" -> {
                val targetWords = allWords.shuffled().take(5)
                targetWords.forEach { targetWord ->
                    val isCorrect = Random.nextBoolean()
                    val displayedDef = if (isCorrect) targetWord.definition
                    else allWords.filter { it.id != targetWord.id }.randomOrNull()?.definition ?: "Unknown concept"

                    questions.add(QuizQuestion(
                        id = targetWord.id.toString(),
                        type = QuestionType.TRUE_FALSE,
                        questionText = "Does \"${targetWord.word}\" stand for:\n\n\"$displayedDef\"?",
                        options = listOf("True", "False"),
                        correctAnswerIndex = if (isCorrect) 0 else 1,
                        explanation = "${targetWord.word} stands for ${targetWord.definition}"
                    ))
                }
            }

            // --- IDIOM & PHRASAL VERB: Boşluk Doldurma ---
            "Idiom", "Phrasal Verb" -> {
                val targetWords = allWords.shuffled().take(5)
                targetWords.forEach { targetWord ->
                    val rawSentence = targetWord.exampleSentence ?: "${targetWord.word} is commonly used in professional meetings."

                    var blankedSentence = rawSentence.replace(
                        Regex("\\b${Regex.escape(targetWord.word)}\\b", RegexOption.IGNORE_CASE),
                        "________"
                    )

                    if (blankedSentence == rawSentence) {
                        blankedSentence = "Complete the phrase for this definition:\n\n\"${targetWord.definition}\"\n\n________"
                    }

                    val options = (allWords.filter { it.id != targetWord.id }.shuffled().take(3).map { it.word } + targetWord.word)
                        .map { it.trim() }.distinct().shuffled()

                    val correctIndex = options.indexOf(targetWord.word.trim())
                    if (correctIndex != -1) {
                        questions.add(QuizQuestion(
                            id = targetWord.id.toString(),
                            type = QuestionType.FILL_IN_THE_BLANK,
                            questionText = blankedSentence,
                            options = options,
                            correctAnswerIndex = correctIndex,
                            targetWord = targetWord.word,
                            explanation = "Definition: ${targetWord.definition}"
                        ))
                    }
                }
            }
        }
        return questions.take(5)
    }

    private fun mapLevelIdToCefr(levelId: String): String = when(levelId) {
        "1" -> "B1"
        "2" -> "B2"
        "3" -> "C1"
        else -> "B1"
    }
}