package com.profylish.data.repository

import com.profylish.data.mapper.toDomainModel
import com.profylish.domain.repository.OccupationRepository
import com.profylish.model.occupation.Occupation
import com.profylish.network.model.NetworkOccupation
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import javax.inject.Inject

class OccupationRepositoryImpl @Inject constructor(
    private val supabaseClient: SupabaseClient
) : OccupationRepository {

    override suspend fun searchOccupations(query: String): List<Occupation> {
        return try {
            val result = supabaseClient.postgrest["occupations"]
                .select(columns = Columns.list("Job_Title_Clean", "SOC_Code", "ONET_Title_Group")) {

                    // 👇 DÜZELTİLEN KISIM BURASI 👇
                    // "ilike" fonksiyonunu çağırıyoruz (araya yazmıyoruz)
                    filter {
                        ilike("Job_Title_Clean", "%$query%")
                    }
                    // 👆 ---------------------- 👆

                    limit(20)
                }
                .decodeList<NetworkOccupation>()

            result.map { it.toDomainModel() }
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

            result.map { it.toDomainModel() }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}