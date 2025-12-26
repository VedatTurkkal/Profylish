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
import kotlinx.serialization.Serializable

@Serializable
data class QuizOccupationDto(
    val ONET_Title_Group: String? = null
)

class DictionaryRepositoryImpl @Inject constructor(
    private val supabaseClient: SupabaseClient
) : DictionaryRepository {

    override suspend fun getWordsForLevel(profession: String, cefrLevel: String): List<DictionaryWord> {
        return try {
            // 1. ADIM (DÜZELTME): Yine sadece gerekli sütunu çekiyoruz.
            val occupationEntry = supabaseClient.postgrest["occupations"]
                .select(columns = Columns.list("ONET_Title_Group")) {
                    filter { eq("Job_Title_Clean", profession) }
                    limit(1)
                }
                .decodeSingleOrNull<QuizOccupationDto>()

            val fullGroupTitle = occupationEntry?.ONET_Title_Group ?: profession

            // 2. Basitleştirme
            val simpleSearchKey = fullGroupTitle.split(",").first().trim()

            Log.d("QUIZ_DEBUG", "Fetching Quiz for '$simpleSearchKey' ($cefrLevel)")

            // 3. Kelimeleri Çek
            var result = supabaseClient.postgrest["dictionary"]
                .select {
                    filter {
                        ilike("source_profession", "%$simpleSearchKey%")
                        eq("source_cefr_level", cefrLevel)
                    }
                }
                .decodeList<NetworkDictionaryEntry>()

            // 4. Fallback
            if (result.isEmpty()) {
                Log.w("QUIZ_DEBUG", "No words for $simpleSearchKey at $cefrLevel. Trying fallback.")
                result = supabaseClient.postgrest["dictionary"]
                    .select {
                        filter {
                            ilike("source_profession", "%Business%")
                            eq("source_cefr_level", cefrLevel)
                        }
                    }
                    .decodeList<NetworkDictionaryEntry>()
            }

            Log.d("QUIZ_DEBUG", "Found ${result.size} words")
            result.map { it.toDomain() }

        } catch (e: Exception) {
            Log.e("QUIZ_DEBUG", "Error fetching quiz data", e)
            emptyList()
        }
    }
}