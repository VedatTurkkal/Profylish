package com.profylish.domain.repository

import com.profylish.model.leaderboard.LeaderboardEntry
import com.profylish.model.leaderboard.LeagueTier
import kotlinx.coroutines.flow.Flow

interface LeaderboardRepository {
    fun getLeaderboard(tier: LeagueTier): Flow<List<LeaderboardEntry>>

    suspend fun getCurrentUserTier(): LeagueTier
}