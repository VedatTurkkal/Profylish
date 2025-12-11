package com.profylish.data.repository

import android.util.Log
import com.profylish.domain.repository.CurriculumRepository
import com.profylish.model.curriculum.LearningUnit
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import javax.inject.Inject

// Network modelini de burada basitçe tanımlayabiliriz veya core/network altına koyabilirsin
@kotlinx.serialization.Serializable
data class NetworkLearningUnit(
    val id: String,
    val title: String,
    val description: String? = null,
    val unit_order: Int,
    val occupation_group: String
)

class CurriculumRepositoryImpl @Inject constructor(
    private val supabaseClient: SupabaseClient
) : CurriculumRepository {

    override suspend fun getCurriculumForOccupation(occupationGroup: String): List<LearningUnit> {
        return try {
            val result = supabaseClient.postgrest["curriculum"] // Tablo adı 'curriculum' olmalı
                .select {
                    filter {
                        // occupation_group kolonuna göre filtrele
                        eq("occupation_group", occupationGroup)
                    }
                    order("unit_order", ascending = true)
                }
                .decodeList<NetworkLearningUnit>()

            result.map {
                LearningUnit(
                    id = it.id,
                    title = it.title,
                    description = it.description,
                    order = it.unit_order,
                    occupationGroup = it.occupation_group
                )
            }
        } catch (e: Exception) {
            Log.e("CurriculumRepo", "Error fetching curriculum", e)
            emptyList()
        }
    }
}