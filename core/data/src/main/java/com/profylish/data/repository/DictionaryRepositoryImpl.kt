@file:OptIn(kotlinx.serialization.InternalSerializationApi::class) // DÜZELTME 1: En tepede olmalı!

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
data class QuizOccupationDto(
    @SerialName("ONET_Title_Group") val onetTitleGroup: String? = null
)

// DÜZELTME 2: @SerialName kullanarak Kotlin standardına (camelCase) geçtik
@Serializable
data class WordProgressDto(
    @SerialName("word_id") val wordId: Int
)

@Serializable
data class InsertWordProgressDto(
    @SerialName("user_id") val userId: String,
    @SerialName("word_id") val wordId: Int,
    @SerialName("is_mastered") val isMastered: Boolean = true
)

class DictionaryRepositoryImpl @Inject constructor(
    private val supabaseClient: SupabaseClient
) : DictionaryRepository {

    override suspend fun getWordsForLevel(
        profession: String,
        cefrLevel: String,
        excludedWordIds: List<Int>
    ): List<DictionaryWord> {
        return try {
            val targetCefrLevels = when (cefrLevel) {
                "I'm Starting from Scratch", "B1" -> listOf("B1", "B2")
                "I Have Some Knowledge", "B2" -> listOf("B2", "C1")
                "I'm Experienced", "C1" -> listOf("C1", "C2")
                "C2" -> listOf("C2")
                else -> listOf("B1", "B2")
            }

            val occupationEntry = supabaseClient.postgrest["occupations"]
                .select(columns = Columns.list("ONET_Title_Group")) {
                    filter { eq("Job_Title_Clean", profession) }
                    limit(1)
                }
                .decodeSingleOrNull<QuizOccupationDto>()

            val fullGroupTitle = occupationEntry?.onetTitleGroup ?: profession
            val simpleSearchKey = fullGroupTitle.split(",").first().trim()

            var result = supabaseClient.postgrest["dictionary"]
                .select {
                    filter {
                        ilike("source_profession", "%$simpleSearchKey%")
                        isIn("source_cefr_level", targetCefrLevels)
                        if (excludedWordIds.isNotEmpty()) {
                            excludedWordIds.forEach { excludedId ->
                                neq("id", excludedId)
                            }
                        }
                    }
                    limit(20)
                }
                .decodeList<NetworkDictionaryEntry>()

            if (result.isEmpty()) {
                result = supabaseClient.postgrest["dictionary"]
                    .select {
                        filter {
                            ilike("source_profession", "%Business%")
                            isIn("source_cefr_level", targetCefrLevels)
                            if (excludedWordIds.isNotEmpty()) {
                                excludedWordIds.forEach { excludedId ->
                                    neq("id", excludedId)
                                }
                            }
                        }
                        limit(10)
                    }
                    .decodeList<NetworkDictionaryEntry>()
            }

            result.map { it.toDomain() }

        } catch (e: Exception) {
            Log.e("QUIZ_DEBUG", "Error fetching quiz data", e)
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
                .map { it.wordId } // Düzeltildi: .wordId
        } catch (e: Exception) {
            Log.e("QUIZ_DEBUG", "Error fetching learned words", e)
            emptyList()
        }
    }

    override suspend fun markWordsAsLearned(userId: String, wordIds: List<Int>) {
        if (wordIds.isEmpty()) return
        try {
            val progressEntries = wordIds.map { id ->
                InsertWordProgressDto(userId = userId, wordId = id) // Düzeltildi
            }

            supabaseClient.postgrest["user_word_progress"]
                .upsert(
                    value = progressEntries,
                    onConflict = "user_id, word_id",
                    ignoreDuplicates = true
                )

            Log.d("QUIZ_DEBUG", "Marked ${wordIds.size} words as learned.")
        } catch (e: Exception) {
            Log.e("QUIZ_DEBUG", "Failed to save progress", e)
        }
    }
}