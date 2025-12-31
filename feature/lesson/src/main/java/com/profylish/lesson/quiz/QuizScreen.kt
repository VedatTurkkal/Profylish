package com.profylish.lesson.quiz

import android.annotation.SuppressLint
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.profylish.lesson.model.QuizUiState

@SuppressLint("MissingPermission") // Manifest'e eklediysen bu annotation uyarıyı susturur
@Composable
fun QuizScreen(
    onBackClick: () -> Unit,
    onNavigateToAuth: () -> Unit,
    viewModel: QuizViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var showQuitDialog by remember { mutableStateOf(false) }
    var showSignUpDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val vibrator = remember(context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(android.content.Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(android.content.Context.VIBRATOR_SERVICE) as Vibrator
        }
    }

    BackHandler { showQuitDialog = true }

    LaunchedEffect(Unit) {
        viewModel.navigationEvent.collect { event ->
            when (event) {
                is QuizNavigationEvent.NavigateToAuth -> showSignUpDialog = true
                is QuizNavigationEvent.NavigateHome -> onBackClick()
                is QuizNavigationEvent.VibrateSuccess -> {
                    // MinSDK 26 olduğu için direkt kullanabiliriz
                    vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
                }
                is QuizNavigationEvent.VibrateError -> {
                    vibrator.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 100, 50, 100), -1))
                }
            }
        }
    }

    // ... (Geri kalan UI kodları aynı, değişiklik yok) ...
    // Hataları önlemek için tam kopyalaman gerekirse eski QuizScreen kodunun UI kısmını buraya yapıştır.
    // Yukarıdaki mantık blokları düzeltilmiştir.

    // --- DIALOGLAR VE UI ---
    if (showQuitDialog) {
        AlertDialog(
            onDismissRequest = { showQuitDialog = false },
            title = { Text("Quit this session?") },
            text = { Text("You will lose all progress in this lesson if you quit now.") },
            confirmButton = {
                TextButton(onClick = {
                    showQuitDialog = false
                    onBackClick()
                }) { Text("QUIT", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showQuitDialog = false }) { Text("CANCEL") }
            }
        )
    }

    if (showSignUpDialog) {
        AlertDialog(
            onDismissRequest = {
                showSignUpDialog = false
                onBackClick()
            },
            title = { Text("Save Your Progress!") },
            text = {
                Text("Great job! Create a free profile now to save your XP, streak, and league ranking permanently.", textAlign = TextAlign.Start)
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSignUpDialog = false
                        onNavigateToAuth()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1CB0F6))
                ) { Text("Create Profile", fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = {
                    showSignUpDialog = false
                    onBackClick()
                }) { Text("Maybe Later", color = Color.Gray) }
            }
        )
    }

    Scaffold(containerColor = Color.White) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            when (val state = uiState) {
                is QuizUiState.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                is QuizUiState.Error -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("⚠️ ${state.message}", color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(16.dp))
                        Button(onClick = onBackClick) { Text("Go Back") }
                    }
                }
                is QuizUiState.Success -> {
                    if (state.isLessonCompleted) {
                        val totalScore = if (state.totalQuestions > 0) state.totalQuestions * 10 else 1
                        val passed = (state.score.toFloat() / totalScore) >= 0.7f
                        LessonCompletedView(score = state.score, passed = passed, onContinueClick = { viewModel.onLessonFinished() })
                    } else {
                        QuizContent(
                            state = state,
                            onOptionSelected = viewModel::onOptionSelected,
                            onCheckAnswer = viewModel::onCheckAnswer,
                            onNextQuestion = viewModel::onNextQuestion,
                            onCloseClick = { showQuitDialog = true }
                        )
                    }
                }
            }
        }
    }
}

// ... (QuizContent ve LessonCompletedView aynı kalacak) ...
@Composable
fun QuizContent(
    state: QuizUiState.Success,
    onOptionSelected: (Int) -> Unit,
    onCheckAnswer: () -> Unit,
    onNextQuestion: () -> Unit,
    onCloseClick: () -> Unit
) {
    val progress by animateFloatAsState(
        targetValue = (state.currentQuestionIndex + 1) / state.totalQuestions.toFloat(),
        label = "ProgressBar"
    )

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onCloseClick) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Gray)
            }
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.weight(1f).height(12.dp).clip(RoundedCornerShape(8.dp)),
                color = Color(0xFF58CC02),
                trackColor = Color(0xFFE5E5E5)
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = state.currentQuestion.questionText,
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = Color(0xFF3C3C3C),
                modifier = Modifier.padding(bottom = 32.dp)
            )

            state.currentQuestion.options.forEachIndexed { index, optionText ->
                QuizOptionCard(
                    text = optionText,
                    isSelected = state.selectedOptionIndex == index,
                    isAnswerChecked = state.isAnswerChecked,
                    isCorrectAnswer = index == state.currentQuestion.correctAnswerIndex,
                    onClick = { onOptionSelected(index) }
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
        }

        val footerColor = if (state.isAnswerChecked) {
            if (state.isAnswerCorrect) Color(0xFFD7FFB8) else Color(0xFFFFDFE0)
        } else {
            Color.White
        }

        Column(
            modifier = Modifier.fillMaxWidth().background(footerColor).padding(16.dp)
        ) {
            if (state.isAnswerChecked) {
                Row(modifier = Modifier.padding(bottom = 16.dp)) {
                    val icon = if (state.isAnswerCorrect) "🎉" else "❌"
                    val title = if (state.isAnswerCorrect) "Excellent!" else "Correct solution:"

                    Text(text = icon, style = MaterialTheme.typography.headlineSmall)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = if (state.isAnswerCorrect) Color(0xFF58CC02) else Color(0xFFEA2B2B)
                        )
                        if (!state.isAnswerCorrect) {
                            Text(
                                text = state.currentQuestion.options[state.currentQuestion.correctAnswerIndex],
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color(0xFFEA2B2B)
                            )
                        }
                    }
                }
            }

            Button(
                onClick = { if (state.isAnswerChecked) onNextQuestion() else onCheckAnswer() },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                enabled = state.selectedOptionIndex != null,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (state.isAnswerChecked && !state.isAnswerCorrect) Color(0xFFEA2B2B) else Color(0xFF58CC02),
                    disabledContainerColor = Color(0xFFE5E5E5)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = if (state.isAnswerChecked) "CONTINUE" else "CHECK",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}

@Composable
fun QuizOptionCard(
    text: String,
    isSelected: Boolean,
    isAnswerChecked: Boolean,
    isCorrectAnswer: Boolean,
    onClick: () -> Unit
) {
    val borderColor = when {
        isAnswerChecked && isCorrectAnswer -> Color(0xFF58CC02)
        isAnswerChecked && isSelected -> Color(0xFFEA2B2B)
        isSelected -> Color(0xFF1CB0F6)
        else -> Color(0xFFE5E5E5)
    }
    val backgroundColor = when {
        isAnswerChecked && isCorrectAnswer -> Color(0xFFD7FFB8)
        isAnswerChecked && isSelected -> Color(0xFFFFDFE0)
        isSelected -> Color(0xFFDDF4FF)
        else -> Color.White
    }
    val textColor = if (!isAnswerChecked && !isSelected) Color(0xFF4B4B4B) else borderColor

    Surface(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).clickable(enabled = !isAnswerChecked, onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(2.dp, borderColor),
        color = backgroundColor
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
            color = textColor
        )
    }
}

@Composable
fun LessonCompletedView(score: Int, passed: Boolean, onContinueClick: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val icon = if (passed) "🎉" else "💪"
        val title = if (passed) "Lesson Complete!" else "Don't give up!"
        val subtitle = if (passed) "You earned $score XP" else "You need 70% to pass. Try again!"
        val btnColor = if (passed) Color(0xFF58CC02) else Color.Gray

        Text(text = icon, style = MaterialTheme.typography.displayLarge)
        Spacer(modifier = Modifier.height(16.dp))
        Text(title, style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold), color = Color(0xFF58CC02))
        Spacer(modifier = Modifier.height(8.dp))
        Text(subtitle, style = MaterialTheme.typography.titleLarge, color = Color(0xFFFFC800))
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = onContinueClick,
            modifier = Modifier.fillMaxWidth(0.8f).height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = btnColor)
        ) {
            Text("CONTINUE")
        }
    }
}