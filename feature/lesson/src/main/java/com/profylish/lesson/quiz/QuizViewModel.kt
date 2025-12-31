package com.profylish.lesson.quiz

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.profylish.domain.repository.AuthRepository
import com.profylish.domain.repository.DictionaryRepository
import com.profylish.domain.repository.UserDataRepository
import com.profylish.domain.usecase.learning.GenerateDailyLessonUseCase
import com.profylish.lesson.model.QuizUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
// QuestionType enumunu import etmeyi unutmayın (Matching logic için)
import com.profylish.model.lesson.QuestionType

sealed interface QuizNavigationEvent {
    data object NavigateToAuth : QuizNavigationEvent
    data object NavigateHome : QuizNavigationEvent
    data object VibrateSuccess : QuizNavigationEvent
    data object VibrateError : QuizNavigationEvent
}

@HiltViewModel
class QuizViewModel @Inject constructor(
    private val generateDailyLessonUseCase: GenerateDailyLessonUseCase,
    private val userDataRepository: UserDataRepository,
    private val dictionaryRepository: DictionaryRepository,
    private val authRepository: AuthRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val levelId: String = savedStateHandle.get<String>("levelId") ?: "1"
    private val profession: String = savedStateHandle.get<String>("profession") ?: "General"
    private val quizCategory: String = savedStateHandle.get<String>("quizCategory") ?: "TERM"
    private val isProgression: Boolean = true

    private val _uiState = MutableStateFlow<QuizUiState>(QuizUiState.Loading)
    val uiState: StateFlow<QuizUiState> = _uiState.asStateFlow()

    private val _navigationEvent = Channel<QuizNavigationEvent>()
    val navigationEvent = _navigationEvent.receiveAsFlow()

    init {
        loadLesson()
    }

    private fun loadLesson() {
        viewModelScope.launch {
            _uiState.value = QuizUiState.Loading
            try {
                val userPrefs = userDataRepository.userData.first()
                val currentHearts = userPrefs.hearts
                val vibrationEnabled = userPrefs.isVibrationEnabled

                // GÜNCELLENDİ: UseCase 3 parametre ile çağrılıyor
                val questions = generateDailyLessonUseCase(profession, levelId, quizCategory)

                if (questions.isNotEmpty()) {
                    _uiState.value = QuizUiState.Success(
                        questions = questions,
                        currentQuestionIndex = 0,
                        totalQuestions = questions.size,
                        currentQuestion = questions[0],
                        score = 0,
                        hearts = currentHearts,
                        isVibrationEnabled = vibrationEnabled
                    )
                } else {
                    _uiState.value = QuizUiState.Error("No content found for $quizCategory in Level $levelId.")
                }
            } catch (e: Exception) {
                _uiState.value = QuizUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun onOptionSelected(index: Int) {
        val currentState = _uiState.value
        if (currentState is QuizUiState.Success && !currentState.isAnswerChecked) {
            _uiState.value = currentState.copy(selectedOptionIndex = index)
        }
    }

    fun onCheckAnswer() {
        val currentState = _uiState.value
        if (currentState is QuizUiState.Success) {

            // --- MATCHING PAIRS ÖZEL KONTROL ---
            if (currentState.currentQuestion.type == QuestionType.MATCHING_PAIRS) {
                // Eşleştirme zaten UI tarafında kontrol ediliyor. Buraya gelindiyse hepsi doğrudur.
                // Puan ver ve geç.
                val newScore = currentState.score + 20 // Matching daha değerli olabilir

                viewModelScope.launch {
                    if (currentState.isVibrationEnabled) _navigationEvent.send(QuizNavigationEvent.VibrateSuccess)
                }

                _uiState.value = currentState.copy(
                    isAnswerChecked = true,
                    isAnswerCorrect = true,
                    score = newScore
                    // Matching için combo mantığı karmaşık olabilir, şimdilik es geçiyoruz
                )

                // Otomatik geçiş için kısa delay (opsiyonel) veya kullanıcı "Continue" diyebilir
                return
            }

            // --- DİĞER SORU TİPLERİ (MC, T/F, FILL) ---
            if (currentState.selectedOptionIndex != null) {
                val isCorrect = currentState.selectedOptionIndex == currentState.currentQuestion.correctAnswerIndex

                viewModelScope.launch {
                    if (currentState.isVibrationEnabled) {
                        if (isCorrect) _navigationEvent.send(QuizNavigationEvent.VibrateSuccess)
                        else _navigationEvent.send(QuizNavigationEvent.VibrateError)
                    }
                    if (!isCorrect) userDataRepository.deductHeart()
                }

                val newCombo = if (isCorrect) currentState.comboStreak + 1 else 0
                val showAnim = isCorrect && newCombo >= 2
                val newHearts = if (!isCorrect) (currentState.hearts - 1).coerceAtLeast(0) else currentState.hearts

                _uiState.value = currentState.copy(
                    isAnswerChecked = true,
                    isAnswerCorrect = isCorrect,
                    score = if (isCorrect) currentState.score + 10 else currentState.score,
                    comboStreak = newCombo,
                    showComboAnim = showAnim,
                    hearts = newHearts
                )

                if (showAnim) {
                    viewModelScope.launch {
                        delay(1500)
                        val current = _uiState.value
                        if (current is QuizUiState.Success) {
                            _uiState.value = current.copy(showComboAnim = false)
                        }
                    }
                }
            }
        }
    }

    fun onNextQuestion() {
        val currentState = _uiState.value
        if (currentState is QuizUiState.Success) {
            val nextIndex = currentState.currentQuestionIndex + 1
            if (nextIndex < currentState.questions.size) {
                _uiState.value = currentState.copy(
                    currentQuestionIndex = nextIndex,
                    currentQuestion = currentState.questions[nextIndex],
                    selectedOptionIndex = null,
                    isAnswerChecked = false,
                    isAnswerCorrect = false,
                    showComboAnim = false
                )
            } else {
                _uiState.value = currentState.copy(isLessonCompleted = true)
            }
        }
    }

    fun onLessonFinished() {
        val currentState = _uiState.value
        if (currentState !is QuizUiState.Success) return

        val totalPossible = currentState.totalQuestions * 10
        // Not: Matching soruları varsa totalPossible hesabı değişebilir (örn: soru sayısı * 20)
        // Basitlik için yüzde hesabı yerine direkt geçiş de verebilirsiniz veya puanı dinamik hesaplayabilirsiniz.

        val percentage = if (totalPossible > 0) (currentState.score.toFloat() / totalPossible) * 100 else 0f

        // Esnek geçiş: Matching soruları puanı şişirebilir, yüzde hesabı > 100 olabilir.
        val passed = percentage >= 70 || (currentState.questions.any { it.type == QuestionType.MATCHING_PAIRS } && currentState.score > 0)

        viewModelScope.launch {
            if (passed) {
                userDataRepository.updateUserStats(xpEarned = currentState.score, gemsEarned = 15)

                if (isProgression) {
                    userDataRepository.unlockNextLevel()
                }

                val userId = authRepository.getCurrentUserId()
                if (userId != null) {
                    val wordIdsUsedInLesson = currentState.questions.mapNotNull {
                        it.id.toString().toIntOrNull()
                    }
                    dictionaryRepository.markWordsAsLearned(userId, wordIdsUsedInLesson)
                }

                if (!authRepository.isUserLoggedIn()) {
                    _navigationEvent.send(QuizNavigationEvent.NavigateToAuth)
                } else {
                    _navigationEvent.send(QuizNavigationEvent.NavigateHome)
                }
            } else {
                _navigationEvent.send(QuizNavigationEvent.NavigateHome)
            }
        }
    }
}