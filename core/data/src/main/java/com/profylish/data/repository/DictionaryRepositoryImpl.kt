@file:OptIn(kotlinx.serialization.InternalSerializationApi::class)

package com.profylish.data.repository

import android.util.Log
import com.profylish.data.mapper.toDomain
import com.profylish.domain.repository.DictionaryRepository
import com.profylish.model.curriculum.DictionaryWord
import com.profylish.network.model.dictionary.NetworkDictionaryEntry
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import javax.inject.Inject
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WordProgressDto(@SerialName("word_id") val wordId: Int)

@Serializable
data class InsertWordProgressDto(
    @SerialName("user_id") val userId: String,
    @SerialName("word_id") val wordId: Int,
    @SerialName("is_mastered") val isMastered: Boolean = true
)

class DictionaryRepositoryImpl @Inject constructor(
    private val supabaseClient: SupabaseClient
) : DictionaryRepository {

    override suspend fun getWordsForLesson(
        profession: String,
        cefrLevel: String, // "B1", "B2", "C1", "C2" veya Onboarding metni
        wordType: String
    ): List<DictionaryWord> {
        return try {
            // Onboarding'den gelen tecrübe seviyelerini DB seviyeleriyle eşleştiriyoruz
            val targetLevels = when (cefrLevel) {
                "I'm Starting from Scratch", "B1" -> listOf("B1", "B2")
                "I Have Some Knowledge", "B2" -> listOf("B2", "C1")
                "I'm Experienced", "C1" -> listOf("C1", "C2")
                "C2" -> listOf("C2")
                else -> listOf("B1", "B2")
            }

            val result = supabaseClient.postgrest["dictionary"]
                .select {
                    filter {
                        ilike("source_profession", "%$profession%")
                        isIn("source_cefr_level", targetLevels)
                        eq("type", wordType)
                    }
                    limit(15)
                }
                .decodeList<NetworkDictionaryEntry>()

            // Eğer sonuç boşsa "Business" genel kategorisinden getir (Fallback)
            if (result.isEmpty()) {
                val fallbackResult = supabaseClient.postgrest["dictionary"]
                    .select {
                        filter {
                            ilike("source_profession", "%Business%")
                            isIn("source_cefr_level", targetLevels)
                            eq("type", wordType)
                        }
                        limit(10)
                    }.decodeList<NetworkDictionaryEntry>()
                return fallbackResult.map { it.toDomain() }
            }

            result.map { it.toDomain() }
        } catch (e: Exception) {
            Log.e("QUIZ_DEBUG", "Error fetching words for $wordType: ${e.message}")
            emptyList()
        }
    }

    override suspend fun getLearnedWordIds(userId: String): List<Int> {
        return try {
            supabaseClient.postgrest["user_word_progress"]
                .select(columns = Columns.list("word_id")) {
                    filter { eq("user_id", userId) }
                }
                .decodeList<WordProgressDto>()
                .map { it.wordId }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun markWordsAsLearned(userId: String, wordIds: List<Int>) {
        if (wordIds.isEmpty()) return
        try {
            val entries = wordIds.map { InsertWordProgressDto(userId, it) }
            supabaseClient.postgrest["user_word_progress"].upsert(entries)
        } catch (e: Exception) {
            Log.e("QUIZ_DEBUG", "Failed to mark words: ${e.message}")
        }
    }

    // Geriye dönük uyumluluk için eski metot
    override suspend fun getWordsForLevel(
        profession: String,
        cefrLevel: String,
        excludedWordIds: List<Int>
    ): List<DictionaryWord> = getWordsForLesson(profession, cefrLevel, "Term")
}