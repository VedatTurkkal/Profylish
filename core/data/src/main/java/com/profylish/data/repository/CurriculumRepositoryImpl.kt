package com.profylish.data.repository

import android.util.Log
import com.profylish.data.mapper.toDomain
import com.profylish.domain.repository.CurriculumRepository
import com.profylish.model.curriculum.LearningUnit
import com.profylish.model.roadmap.NodeStatus
import com.profylish.model.roadmap.NodeType
import com.profylish.model.roadmap.RoadmapNode
import com.profylish.network.model.dictionary.NetworkDictionaryEntry
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import javax.inject.Inject
import kotlinx.serialization.Serializable

// Sadece grup ismini çekmek için basit bir model
@Serializable
data class RoadmapOccupationDto(
    val ONET_Title_Group: String? = null
)

class CurriculumRepositoryImpl @Inject constructor(
    private val supabaseClient: SupabaseClient
) : CurriculumRepository {

    override suspend fun getCurriculumForOccupation(occupationGroup: String): List<LearningUnit> {
        return emptyList()
    }

    override suspend fun generateRoadmap(occupationTitle: String, currentLevel: Int): List<RoadmapNode> {
        return try {
            Log.d("ROADMAP_DEBUG", "Generating roadmap for: $occupationTitle (Current Level: $currentLevel)")

            // 1. ADIM (DÜZELTME): Sadece 'ONET_Title_Group' sütununu çek.
            // Böylece 'ISCO-08' gibi null olan sütunlar gelmez ve hata oluşmaz.
            val occupationEntry = supabaseClient.postgrest["occupations"]
                .select(columns = Columns.list("ONET_Title_Group")) {
                    filter { eq("Job_Title_Clean", occupationTitle) }
                    limit(1)
                }
                .decodeSingleOrNull<RoadmapOccupationDto>()

            val fullGroupTitle = occupationEntry?.ONET_Title_Group ?: occupationTitle

            // 2. Basitleştirilmiş Arama
            val simpleSearchKey = fullGroupTitle.split(",").first().trim()

            Log.d("ROADMAP_DEBUG", "Original: '$fullGroupTitle' -> Searching for: '$simpleSearchKey'")

            var networkWords = supabaseClient.postgrest["dictionary"]
                .select {
                    filter {
                        ilike("source_profession", "%$simpleSearchKey%")
                    }
                }
                .decodeList<NetworkDictionaryEntry>()

            Log.d("ROADMAP_DEBUG", "Found ${networkWords.size} words.")

            // 3. Fallback
            if (networkWords.isEmpty()) {
                Log.w("ROADMAP_DEBUG", "Still empty. Switching to fallback 'General Business'.")
                networkWords = supabaseClient.postgrest["dictionary"]
                    .select {
                        filter { ilike("source_profession", "%Business%") }
                    }
                    .decodeList<NetworkDictionaryEntry>()
            }

            // 4. Node Oluşturma
            val words = networkWords.map { it.toDomain() }

            if (words.isEmpty()) {
                Log.e("ROADMAP_DEBUG", "Roadmap generation failed. Database might be empty.")
                return emptyList()
            }

            val chunks = words.chunked(7)

            val nodes = chunks.mapIndexed { index, _ ->
                val nodeLevel = index + 1

                val status = when {
                    nodeLevel < currentLevel -> NodeStatus.COMPLETED
                    nodeLevel == currentLevel -> NodeStatus.ACTIVE
                    else -> NodeStatus.LOCKED
                }

                val type = if (nodeLevel % 5 == 0) NodeType.CHEST else NodeType.LESSON

                RoadmapNode(
                    id = nodeLevel.toString(),
                    title = "Level $nodeLevel",
                    status = status,
                    type = type,
                    stars = if (status == NodeStatus.COMPLETED) 3 else 0
                )
            }

            nodes

        } catch (e: Exception) {
            Log.e("SUPABASE_ERROR", "Error generating roadmap", e)
            e.printStackTrace()
            emptyList()
        }
    }
}