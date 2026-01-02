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

        val dbType = when(category) {
            "TERM" -> "Term"
            "IDIOM" -> "Idiom"
            "PHRASAL_VERB" -> "Phrasal Verb"
            "ACRONYM" -> "Acronym"
            else -> "Term"
        }

        val words = dictionaryRepository.getWordsForLesson(profession, baseCefr, dbType)
        if (words.isEmpty()) return emptyList()

        val questions = mutableListOf<QuizQuestion>()

        when (dbType) {
            "Term" -> {
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
                    val rawSentence = word.exampleSentence ?: "${word.word} is commonly used in professional meetings."

                    // 1. Kelimeyi (büyük/küçük harf duyarsız) bul ve boşluk yap
                    var blankedSentence = rawSentence.replace(
                        Regex("\\b${Regex.escape(word.word)}\\b", RegexOption.IGNORE_CASE),
                        "________"
                    )

                    // 2. Eğer kelime çekimlenmişse (örn: 'setting up' vs 'Set up') ve değişmediyse,
                    //    soruyu 'Tanım' sorusuna çevir. Cevabı açık etme.
                    if (blankedSentence == rawSentence) {
                        blankedSentence = "Complete the phrase for this definition:\n\n\"${word.definition}\"\n\n________"
                    }

                    // 3. Şıkları hazırla, temizle ve TEKRARLARI ÖNLE (.distinct())
                    val distractors = words.filter { it.id != word.id }
                        .shuffled()
                        .take(3)
                        .map { it.word }

                    val options = (distractors + word.word)
                        .map { it.trim() } // Boşlukları temizle
                        .distinct()        // Aynı şıkkı sil
                        .shuffled()

                    // Doğru cevabın index'ini bul (trimlenmiş haliyle)
                    val correctIndex = options.indexOf(word.word.trim())

                    // Eğer distinct yüzünden bir şeyler ters gittiyse ve doğru cevap listede yoksa (çok düşük ihtimal ama güvenlik)
                    if (correctIndex != -1) {
                        questions.add(QuizQuestion(
                            id = word.id.toString(),
                            type = QuestionType.FILL_IN_THE_BLANK,
                            questionText = blankedSentence,
                            options = options,
                            correctAnswerIndex = correctIndex,
                            targetWord = word.word,
                            explanation = "Definition: ${word.definition}"
                        ))
                    }
                }
            }
        }
        return questions
    }

    private fun createMCQuestions(words: List<com.profylish.model.curriculum.DictionaryWord>, list: MutableList<QuizQuestion>) {
        words.forEach { w ->
            val distractors = words.filter { it.id != w.id }.shuffled().take(3).map { it.word }
            val options = (distractors + w.word)
                .map { it.trim() }
                .distinct() // Burada da tekrarı önle
                .shuffled()

            val correctIndex = options.indexOf(w.word.trim())

            if (correctIndex != -1) {
                list.add(QuizQuestion(
                    id = w.id.toString(),
                    type = QuestionType.MULTIPLE_CHOICE,
                    questionText = "Which term matches this definition?\n\n${w.definition}",
                    options = options,
                    correctAnswerIndex = correctIndex
                ))
            }
        }
    }

    private fun mapLevelIdToCefr(levelId: String): String = when(levelId) {
        "1" -> "B1"
        "2" -> "B2"
        "3" -> "C1"
        else -> "B1"
    }
}