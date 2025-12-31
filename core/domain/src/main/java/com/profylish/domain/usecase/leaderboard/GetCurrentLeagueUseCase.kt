package com.profylish.domain.usecase.leaderboard

import com.profylish.domain.repository.LeaderboardRepository
import com.profylish.model.leaderboard.LeaderboardEntry
import com.profylish.model.leaderboard.LeagueTier
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class GetCurrentLeagueUseCase @Inject constructor(
    private val leaderboardRepository: LeaderboardRepository
) {
    operator fun invoke(): Flow<Pair<LeagueTier, List<LeaderboardEntry>>> = flow {
        val currentTier = leaderboardRepository.getCurrentUserTier()

        leaderboardRepository.getLeaderboard(currentTier)
            .collect { list ->
                emit(Pair(currentTier, list))
            }
    }
}