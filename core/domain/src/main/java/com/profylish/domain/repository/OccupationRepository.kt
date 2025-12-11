package com.profylish.domain.repository

import com.profylish.model.occupation.Occupation

interface OccupationRepository {

    suspend fun searchOccupations(query: String): List<Occupation>

    suspend fun getPopularOccupations(): List<Occupation>
}