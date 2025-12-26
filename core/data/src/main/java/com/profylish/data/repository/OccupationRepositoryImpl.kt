package com.profylish.data.repository

import com.profylish.domain.repository.OccupationRepository
import com.profylish.model.occupation.Occupation
import com.profylish.model.occupation.OccupationSource
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import javax.inject.Inject
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NetworkOccupation(
    @SerialName("Job_Title_Clean") val jobTitle: String,
    @SerialName("SOC_Code") val socCode: String? = null,
    @SerialName("ONET_Title_Group") val onetGroup: String? = null
)

class OccupationRepositoryImpl @Inject constructor(
    private val supabaseClient: SupabaseClient
) : OccupationRepository {

    override suspend fun searchOccupations(query: String): List<Occupation> {
        return try {
            // Explicitly select only the columns we map to NetworkOccupation
            val result = supabaseClient.postgrest["occupations"]
                .select(columns = Columns.list("Job_Title_Clean", "SOC_Code", "ONET_Title_Group")) {
                    filter {
                        ilike("Job_Title_Clean", "%$query%")
                    }
                    limit(20)
                }
                .decodeList<NetworkOccupation>()

            result.map { it.toDomain() }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    override suspend fun getPopularOccupations(): List<Occupation> {
        return try {
            val result = supabaseClient.postgrest["occupations"]
                .select(columns = Columns.list("Job_Title_Clean", "SOC_Code", "ONET_Title_Group")) {
                    limit(10)
                }
                .decodeList<NetworkOccupation>()

            result.map { it.toDomain() }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    private fun NetworkOccupation.toDomain(): Occupation {
        return Occupation(
            id = this.socCode ?: "unknown",
            title = this.jobTitle,
            description = "Essential vocabulary and concepts for ${this.jobTitle}.",
            category = this.onetGroup ?: "General",
            source = OccupationSource.UNKNOWN
        )
    }
}