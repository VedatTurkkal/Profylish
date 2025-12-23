package com.profylish.data.repository

import com.profylish.domain.repository.CurriculumRepository
import com.profylish.model.curriculum.DictionaryWord
import com.profylish.model.curriculum.LearningUnit
import com.profylish.model.roadmap.RoadmapNode
import com.profylish.model.roadmap.NodeStatus
import com.profylish.model.roadmap.NodeType
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import javax.inject.Inject

class CurriculumRepositoryImpl @Inject constructor(
    private val supabaseClient: SupabaseClient
) : CurriculumRepository {

    override suspend fun getCurriculumForOccupation(occupationGroup: String): List<LearningUnit> {
        // Eski metodun (Şimdilik boş kalabilir veya eski mantığı koruyabilirsin)
        return emptyList()
    }

    // ✅ DÜZELTİLDİ: Artık List<Any> değil, List<RoadmapNode> dönüyor.
    override suspend fun generateRoadmap(occupationTitle: String): List<RoadmapNode> {
        return try {
            // 1. Supabase'den kelimeleri çek
            // Not: 'dictionary' tablosunda 'source_profession' sütunu olduğundan emin ol.
            val words = supabaseClient.postgrest["dictionary"]
                .select {
                    filter {
                         //Meslek ismine göre arama (Büyük/küçük harf duyarsız)
                        ilike("source_profession", "%$occupationTitle%")
                    }
                }
                .decodeList<DictionaryWord>()

            if (words.isEmpty()) return emptyList()

            // 2. Kelimeleri 7'şerli gruplara (Derslere) böl
            val chunks = words.chunked(7)

            // 3. RoadmapNode listesine çevir ve DÖNDÜR
            chunks.mapIndexed { index, _ ->
                val level = index + 1

                // İlk ders açık, diğerleri kilitli
                val status = if (index == 0) NodeStatus.ACTIVE else NodeStatus.LOCKED

                // Her 5. seviye sandık (Chest) olsun
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
            android.util.Log.e("SUPABASE_ERROR", "Veri çekilirken hata oluştu!", e)
            e.printStackTrace()
            emptyList()
        }
    }
}