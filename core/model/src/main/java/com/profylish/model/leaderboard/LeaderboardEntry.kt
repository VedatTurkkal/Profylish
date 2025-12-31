package com.profylish.model.leaderboard

data class LeaderboardEntry(
    val userId: String,
    val username: String,
    val avatarUrl: String?,
    val xp: Int,
    val rank: Int,
    val trend: RankTrend,
    val isCurrentUser: Boolean = false,
)