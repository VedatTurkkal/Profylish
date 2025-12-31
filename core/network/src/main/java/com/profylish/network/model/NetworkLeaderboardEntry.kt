package com.profylish.network.model.leaderboard

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NetworkLeaderboardEntry(
    @SerialName("id") val userId: String,
    @SerialName("username") val username: String?,
    @SerialName("avatar_url") val avatarUrl: String?,
    @SerialName("xp") val xp: Int,
    @SerialName("league_tier") val tier: String
)