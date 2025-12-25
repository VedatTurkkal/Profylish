package com.profylish.data.repository

import com.profylish.data.mapper.toDomain // <-- Mapper eklendi
import com.profylish.domain.repository.CurriculumRepository
import com.profylish.model.curriculum.LearningUnit
import com.profylish.model.roadmap.RoadmapNode
import com.profylish.model.roadmap.NodeStatus
import com.profylish.model.roadmap.NodeType
import com.profylish.network.model.dictionary.NetworkDictionaryEntry // <-- Network Modeli eklendi
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import javax.inject.Inject

class CurriculumRepositoryImpl @Inject constructor(
    private val supabaseClient: SupabaseClient
) : CurriculumRepository {

    override suspend fun getCurriculumForOccupation(occupationGroup: String): List<LearningUnit> {
        return emptyList()
    }

    override suspend fun generateRoadmap(occupationTitle: String): List<RoadmapNode> {
        return try {
            // 1. Supabase'den HAM VERİYİ (NetworkDictionaryEntry) çekiyoruz
            // Çünkü veritabanında ID 'Int' tipinde.
            val networkWords = supabaseClient.postgrest["dictionary"]
                .select {
                    filter {
                        // Meslek ismine göre arama
                        ilike("source_profession", "%$occupationTitle%")
                    }
                }
                .decodeList<NetworkDictionaryEntry>() // <-- KRİTİK DÜZELTME BURADA

            // 2. Ham veriyi Domain modeline (DictionaryWord) çeviriyoruz
            // Burada ID 'String'e dönüşüyor.
            val words = networkWords.map { it.toDomain() }

            if (words.isEmpty()) return emptyList()

            // 3. Yol haritası oluşturma mantığı (Aynı kalıyor)
            val chunks = words.chunked(7)

            chunks.mapIndexed { index, _ ->
                val level = index + 1

                val status = if (index == 0) NodeStatus.ACTIVE else NodeStatus.LOCKED
                val type = if (level % 5 == 0) NodeType.CHEST else NodeType.LESSON

                RoadmapNode(
                    id = level.toString(),
                    title = "Level $level",
                    status = status,
                    type = type,
                    stars = 0
                )
            }
        } catch (e: Exception) {
            android.util.Log.e("SUPABASE_ERROR", "There is an error to fetching data!", e)
            e.printStackTrace()
            emptyList()
        }
    }
}