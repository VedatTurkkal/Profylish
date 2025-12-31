package com.profylish.domain.usecase.learning

import com.profylish.domain.repository.AuthRepository
import com.profylish.domain.repository.DictionaryRepository
import com.profylish.model.lesson.QuestionType
import com.profylish.model.lesson.QuizQuestion
import javax.inject.Inject

class GenerateDailyLessonUseCase @Inject constructor(
    private val dictionaryRepository: DictionaryRepository,
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(profession: String, levelId: String): List<QuizQuestion> {

        // 1. User ID al (Giriş yapmamışsa null)
        val userId = authRepository.getCurrentUserId()

        // 2. Kullanıcının daha önce öğrendiği kelime ID'lerini al
        val learnedWordIds = if (userId != null) {
            dictionaryRepository.getLearnedWordIds(userId)
        } else {
            emptyList()
        }

        // 3. Roadmap Level ID'yi CEFR koduna çevir (1 -> B1)
        val dbLevel = mapLevelIdToCefr(levelId)

        // 4. Repository'den kelimeleri iste (Bildiğimiz kelimeleri HARİÇ tutarak)
        var allWords = dictionaryRepository.getWordsForLevel(
            profession = profession,
            cefrLevel = dbLevel,
            excludedWordIds = learnedWordIds
        )

        // 5. TEKRAR MODU KONTROLÜ
        // Eğer hiç yeni kelime gelmediyse (allWords boş) AMA kullanıcının bildiği kelimeler varsa,
        // demek ki bu seviyedeki havuzu bitirdi. Tekrar amaçlı eski kelimeleri soralım.
        if (allWords.isEmpty() && learnedWordIds.isNotEmpty()) {
            allWords = dictionaryRepository.getWordsForLevel(
                profession = profession,
                cefrLevel = dbLevel,
                excludedWordIds = emptyList() // Filtreyi kaldırdık
            )
        }

        if (allWords.isEmpty()) return emptyList()

        // 6. Ders için 5 kelime seç
        val lessonWords = allWords.shuffled().take(5)
        val questions = mutableListOf<QuizQuestion>()

        // 7. Soru Üretimi
        lessonWords.forEachIndexed { index, targetWord ->
            // Distractor'ları (yanlış şıklar) havuzdan seç
            val distractors = allWords
                .filter { it.id != targetWord.id }
                .shuffled()
                .take(3)
                .map { it.word }

            if (distractors.size < 3) return@forEachIndexed

            val hasExample = !targetWord.exampleSentence.isNullOrBlank()
            val questionType = if (hasExample && index % 2 == 0) {
                QuestionType.FILL_IN_THE_BLANK
            } else {
                QuestionType.MULTIPLE_CHOICE
            }

            val question = when (questionType) {
                QuestionType.MULTIPLE_CHOICE -> {
                    val options = (distractors + targetWord.word).shuffled()
                    QuizQuestion(
                        id = targetWord.id,
                        type = QuestionType.MULTIPLE_CHOICE,
                        questionText = "Which term matches this definition?\n\n\"${targetWord.definition}\"",
                        options = options,
                        correctAnswerIndex = options.indexOf(targetWord.word),
                        explanation = "${targetWord.word}: ${targetWord.definition}"
                    )
                }
                QuestionType.FILL_IN_THE_BLANK -> {
                    val sentence = targetWord.exampleSentence!!
                    val blankedSentence = sentence.replace(
                        Regex("\\b${Regex.escape(targetWord.word)}\\b", RegexOption.IGNORE_CASE),
                        "______"
                    )

                    // Fallback to MC if replace fails
                    if (blankedSentence == sentence) {
                        val options = (distractors + targetWord.word).shuffled()
                        QuizQuestion(
                            id = targetWord.id,
                            type = QuestionType.MULTIPLE_CHOICE,
                            questionText = "Which term matches this definition?\n\n\"${targetWord.definition}\"",
                            options = options,
                            correctAnswerIndex = options.indexOf(targetWord.word),
                            explanation = "${targetWord.word}: ${targetWord.definition}"
                        )
                    } else {
                        val options = (distractors + targetWord.word).shuffled()
                        QuizQuestion(
                            id = targetWord.id,
                            type = QuestionType.FILL_IN_THE_BLANK,
                            questionText = "Complete the sentence:\n\n\"$blankedSentence\"",
                            options = options,
                            correctAnswerIndex = options.indexOf(targetWord.word),
                            explanation = "Full sentence: $sentence"
                        )
                    }
                }
                else -> null
            }

            if (question != null) {
                questions.add(question)
            }
        }

        return questions
    }

    private fun mapLevelIdToCefr(levelId: String): String {
        return when (levelId) {
            "1" -> "B1"
            "2" -> "B2"
            "3" -> "C1"
            "4" -> "C2"
            else -> "B1"
        }
    }
}