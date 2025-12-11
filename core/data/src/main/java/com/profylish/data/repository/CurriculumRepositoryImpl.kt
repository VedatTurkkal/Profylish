package com.profylish.data.repository

import android.util.Log
import com.profylish.domain.repository.CurriculumRepository
import com.profylish.model.curriculum.LearningUnit
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import javax.inject.Inject

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
            val result = supabaseClient.postgrest["curriculum"]
                .select {
                    filter {
                        eq("occupation_group", occupationGroup)
                    }
                    order("unit_order", order = Order.ASCENDING)
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