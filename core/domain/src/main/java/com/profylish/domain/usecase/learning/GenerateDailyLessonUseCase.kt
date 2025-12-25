package com.profylish.domain.usecase.learning

import com.profylish.domain.repository.DictionaryRepository
import com.profylish.model.lesson.QuestionType
import com.profylish.model.lesson.QuizQuestion
import javax.inject.Inject

class GenerateDailyLessonUseCase @Inject constructor(
    private val dictionaryRepository: DictionaryRepository
) {
    /**
     * @param profession: Kullanıcının seçtiği meslek (Örn: "Software Engineer")
     * @param levelId: UI'dan gelen seviye ID'si (Örn: "1", "2", "3")
     */
    suspend operator fun invoke(profession: String, levelId: String): List<QuizQuestion> {

        // 1. ADIM: UI'dan gelen "1" değerini Veritabanındaki "B1" değerine çevir
        val dbLevel = mapLevelIdToCefr(levelId)

        // 2. ADIM: Veritabanından o meslek ve seviyeye ait kelimeleri çek
        val allWords = dictionaryRepository.getWordsForLevel(profession, dbLevel)

        // Eğer kelime yoksa boş dön (ViewModel hata gösterecek)
        if (allWords.isEmpty()) return emptyList()

        // 3. ADIM: Ders için rastgele 10 hedef kelime seç
        // (Eğer toplam kelime sayısı 10'dan azsa, hepsini alır)
        val lessonWords = allWords.shuffled().take(10)

        val questions = mutableListOf<QuizQuestion>()

        // 4. ADIM: Soruları Oluştur
        lessonWords.forEachIndexed { index, targetWord ->

            // Yanlış Şıklar (Distractors):
            // Hedef kelime hariç, havuzdan rastgele 3 kelime seçiyoruz.
            // map { it.word } diyerek sadece kelime metinlerini alıyoruz.
            val distractors = allWords
                .filter { it.id != targetWord.id }
                .shuffled()
                .take(3)
                .map { it.word }

            // Eğer yeterince yanlış şık çıkmazsa (veritabanı çok boşsa) bu soruyu atla
            if (distractors.size < 3) return@forEachIndexed

            // Soru Tipini Belirle:
            // Örnek cümle varsa ve sıra çift sayıysa (%50 ihtimal) Boşluk Doldurma yap.
            val hasExample = !targetWord.exampleSentence.isNullOrBlank()
            val questionType = if (hasExample && index % 2 == 0) {
                QuestionType.FILL_IN_THE_BLANK
            } else {
                QuestionType.MULTIPLE_CHOICE
            }

            val question = when (questionType) {
                QuestionType.MULTIPLE_CHOICE -> {
                    // --- TİP 1: TANIMDAN KELİME BULMA ---
                    // Şıkları oluştur: 3 yanlış + 1 doğru
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
                    // --- TİP 2: CÜMLEDE BOŞLUK DOLDURMA ---
                    // Cümleyi al (hasExample true olduğu için !! güvenli)
                    val sentence = targetWord.exampleSentence!!

                    // Hedef kelimeyi (Büyük/Küçük harf fark etmeksizin) "______" ile değiştir
                    val blankedSentence = sentence.replace(
                        Regex("\\b${Regex.escape(targetWord.word)}\\b", RegexOption.IGNORE_CASE),
                        "______"
                    )

                    // Eğer replace işlemi başarısız olduysa (kelime cümlede tam geçmiyorsa),
                    // fallback olarak Çoktan Seçmeli soru üret (Güvenlik önlemi)
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

                else -> null // İleride Eşleştirme (MATCHING) eklenebilir
            }

            if (question != null) {
                questions.add(question)
            }
        }

        return questions
    }

    /**
     * UI'dan gelen Level ID'yi (1, 2, 3) Veritabanı formatına (B1, B2, C1) çevirir.
     * Bu fonksiyonu veritabanındaki "source_cefr_level" sütunundaki değerlere göre güncellemelisin.
     */
    private fun mapLevelIdToCefr(levelId: String): String {
        return when (levelId) {
            "1" -> "B1" // Başlangıç
            "2" -> "B2" // Orta
            "3" -> "C1" // İleri
            "4" -> "C2" // Uzman
            else -> "B1" // Hata durumunda varsayılan
        }
    }
}