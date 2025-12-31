package com.profylish.data.repository

import com.profylish.domain.repository.LeaderboardRepository
import com.profylish.model.leaderboard.LeaderboardEntry
import com.profylish.model.leaderboard.LeagueTier
import com.profylish.model.leaderboard.RankTrend
import com.profylish.network.model.leaderboard.NetworkLeaderboardEntry
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class LeaderboardRepositoryImpl @Inject constructor(
    private val supabaseClient: SupabaseClient
) : LeaderboardRepository {

    override fun getLeaderboard(tier: LeagueTier): Flow<List<LeaderboardEntry>> = flow {
        val networkList = supabaseClient.from("profiles")
            .select {
                filter {
                    eq("league_tier", tier.name)
                }
                order("xp", order = Order.DESCENDING)
                limit(50)
            }.decodeList<NetworkLeaderboardEntry>()

        val currentUserId = supabaseClient.auth.currentUserOrNull()?.id

        val domainList = networkList.mapIndexed { index, item ->
            LeaderboardEntry(
                userId = item.userId,
                rank = index + 1,
                username = item.username ?: "Unknown User",
                avatarUrl = item.avatarUrl,
                xp = item.xp,
                trend = RankTrend.STABLE,
                isCurrentUser = (item.userId == currentUserId)
            )
        }

        emit(domainList)
    }

    override suspend fun getCurrentUserTier(): LeagueTier {
        val userId = supabaseClient.auth.currentUserOrNull()?.id ?: return LeagueTier.BRONZE

        return try {
            val result = supabaseClient.from("profiles")
                .select {
                    filter { eq("id", userId) }
                }.decodeSingle<NetworkLeaderboardEntry>()

            LeagueTier.valueOf(result.tier)
        } catch (_: Exception) {
            LeagueTier.BRONZE
        }
    }
}