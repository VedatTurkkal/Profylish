package com.profylish.chat

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.ai.client.generativeai.Chat
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.google.ai.client.generativeai.type.generationConfig
import com.profylish.domain.repository.UserDataRepository
import com.profylish.model.user.UserPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val userDataRepository: UserDataRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private val config = generationConfig {
        responseMimeType = "application/json"
    }

    private val jsonParser = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    private var chat: Chat? = null

    init {
        initializeChatOptimized()
    }

    private fun initializeChatOptimized() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            try {
                if (BuildConfig.GEMINI_API_KEY.isBlank()) {
                    throw Exception("API Key is missing")
                }

                val userPrefs: UserPreferences = userDataRepository.userData.first()
                val profession = userPrefs.activeCourseId ?: "General Professional"

                // --- GÜNCELLENMİŞ VE AKILLI SYSTEM INSTRUCTION ---
                val systemPrompt = content {
                    text("""
                        You are a strict professional interviewer for a '$profession' position.
                        
                        CRITICAL RULES:
                        1. LANGUAGE: Speak ONLY ENGLISH.
                        2. FORMAT: Always respond in JSON.
                        
                        SCORING LOGIC (VERY IMPORTANT):
                        - GREETINGS/SHORT PHRASES: If the user says "Hello", "Hi", "Thanks", "Okay", or purely introduces themselves (e.g., "I am Ali"), the score MUST be 0. Do NOT reward simple interactions.
                        - REAL ANSWERS: Only give a score > 75 if the user provides a substantial, relevant answer to an actual interview question.
                        - GRAMMAR: "Hello" is perfect grammar but it is NOT a detailed answer. Score it 0.
                        
                        JSON SCHEMA:
                        {
                            "feedback": "Your response...",
                            "score": (0-100 integer),
                            "is_satisfactory": (true if score > 75),
                            "is_off_topic": (true if non-English or irrelevant)
                        }
                    """.trimIndent())
                }

                val generativeModel = GenerativeModel(
                    modelName = "gemini-2.5-flash",
                    apiKey = BuildConfig.GEMINI_API_KEY,
                    generationConfig = config,
                    systemInstruction = systemPrompt
                )

                chat = generativeModel.startChat()

                val localGreeting = "Hello! I am your AI Interviewer for the $profession position. This interview will be in English. Please introduce yourself briefly."

                _uiState.value = _uiState.value.copy(
                    messages = listOf(ChatMessage(text = localGreeting, isUser = false)),
                    isLoading = false
                )

            } catch (e: Exception) {
                Log.e("GEMINI_INIT", "Error", e)
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    fun sendMessage(userText: String) {
        if (userText.isBlank() || chat == null || _uiState.value.isFinished) return

        val currentMessages = _uiState.value.messages + ChatMessage(text = userText, isUser = true)
        _uiState.value = _uiState.value.copy(messages = currentMessages, isLoading = true)

        viewModelScope.launch {
            try {
                val response = chat!!.sendMessage(userText)
                val responseText = response.text ?: "{}"

                Log.d("GEMINI_RAW", responseText)

                val cleanJson = responseText.replace("```json", "").replace("```", "").trim()
                val evaluation = try {
                    jsonParser.decodeFromString<AiEvaluationResponse>(cleanJson)
                } catch (e: Exception) {
                    AiEvaluationResponse("System error. Let's continue.", 0, false, false)
                }

                var currentStrikes = _uiState.value.strikeCount
                var finalFeedback = evaluation.feedback
                var isFinished = false

                if (evaluation.isOffTopic) {
                    currentStrikes += 1
                    finalFeedback += "\n⚠️ Warning ($currentStrikes/3): Please stay on topic and speak English."
                }

                if (currentStrikes >= 3) {
                    finalFeedback = "⛔ Interview Terminated. You have deviated from the topic or language rules too many times."
                    isFinished = true
                }

                val aiMessage = ChatMessage(text = finalFeedback, isUser = false)

                var showReward = false
                var earnedGems = 0
                if (!isFinished && !evaluation.isOffTopic && evaluation.score > 75) {
                    showReward = true
                    earnedGems = 10
                    // userDataRepository.addGems(10)
                }

                _uiState.value = _uiState.value.copy(
                    messages = currentMessages + aiMessage,
                    isLoading = false,
                    showRewardDialog = showReward,
                    lastScore = evaluation.score,
                    earnedGems = earnedGems,
                    strikeCount = currentStrikes,
                    isFinished = isFinished
                )

            } catch (e: Exception) {
                Log.e("GEMINI_SEND", "Error", e)
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    fun dismissRewardDialog() {
        _uiState.value = _uiState.value.copy(showRewardDialog = false)
    }
}