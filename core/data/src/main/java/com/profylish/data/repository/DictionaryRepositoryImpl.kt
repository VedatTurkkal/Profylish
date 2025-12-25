package com.profylish.data.repository

import com.profylish.data.mapper.toDomain
import com.profylish.domain.repository.DictionaryRepository
import com.profylish.model.curriculum.DictionaryWord
import com.profylish.network.model.dictionary.NetworkDictionaryEntry
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import javax.inject.Inject

class DictionaryRepositoryImpl @Inject constructor(
    private val supabaseClient: SupabaseClient
) : DictionaryRepository {

    override suspend fun getWordsForLevel(profession: String, cefrLevel: String): List<DictionaryWord> {
        return try {
            val result = supabaseClient.postgrest["dictionary"]
                .select {
                    filter {
                        eq("source_profession", profession)
                        eq("source_cefr_level", cefrLevel)
                    }
                }
                .decodeList<NetworkDictionaryEntry>()

            result.map { it.toDomain() }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}