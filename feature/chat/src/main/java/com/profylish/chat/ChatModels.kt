package com.profylish.chat

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class AiEvaluationResponse(
    @SerialName("feedback") val feedback: String,
    @SerialName("score") val score: Int,
    @SerialName("is_satisfactory") val isSatisfactory: Boolean,
    @SerialName("is_off_topic") val isOffTopic: Boolean
)

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val isUser: Boolean
)

data class ChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    val isLoading: Boolean = false,
    val isFinished: Boolean = false,
    val showRewardDialog: Boolean = false,
    val lastScore: Int? = null,
    val earnedGems: Int = 0,
    val strikeCount: Int = 0
)