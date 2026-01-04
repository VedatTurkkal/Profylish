package com.profylish.lesson.quiz

import android.annotation.SuppressLint
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.profylish.lesson.model.QuizUiState
import com.profylish.lesson.quiz.components.QuizContent
import com.profylish.lesson.summary.LessonCompletedView
import com.profylish.ui.components.RavenMascot
import com.profylish.ui.components.RavenState

@SuppressLint("MissingPermission")
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
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
                    } else {
                        @Suppress("DEPRECATION")
                        vibrator.vibrate(50)
                    }
                }
                is QuizNavigationEvent.VibrateError -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        vibrator.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 100, 50, 100), -1))
                    } else {
                        @Suppress("DEPRECATION")
                        vibrator.vibrate(300)
                    }
                }
            }
        }
    }

    // --- DIALOGS ---
    if (uiState is QuizUiState.Success && (uiState as QuizUiState.Success).isHeartsDepleted) {
        AlertDialog(
            onDismissRequest = { onBackClick() },
            title = { Text(text = "Out of Hearts!", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error) },
            text = { Text("You made too many mistakes. Practice makes perfect, try again later!", color = MaterialTheme.colorScheme.onSurface) },
            icon = {
                RavenMascot(state = RavenState.CONFUSED, modifier = Modifier.size(60.dp))
            },
            confirmButton = {
                Button(
                    onClick = { onBackClick() },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("END SESSION", color = Color.White)
                }
            },
            containerColor = MaterialTheme.colorScheme.surface
        )
    }

    if (showQuitDialog) {
        AlertDialog(
            onDismissRequest = { showQuitDialog = false },
            title = { Text("Quit this session?", color = MaterialTheme.colorScheme.onSurface) },
            text = { Text("You will lose all progress in this lesson if you quit now.", color = MaterialTheme.colorScheme.onSurfaceVariant) },
            confirmButton = {
                TextButton(onClick = {
                    showQuitDialog = false
                    onBackClick()
                }) { Text("QUIT", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showQuitDialog = false }) { Text("CANCEL", color = MaterialTheme.colorScheme.primary) }
            },
            containerColor = MaterialTheme.colorScheme.surface
        )
    }

    if (showSignUpDialog) {
        AlertDialog(
            onDismissRequest = { showSignUpDialog = false; onBackClick() },
            title = { Text("Save Your Progress!", color = MaterialTheme.colorScheme.onSurface) },
            text = { Text("Great job! Create a free profile now to save your XP, streak, and league ranking permanently.", color = MaterialTheme.colorScheme.onSurfaceVariant) },
            confirmButton = {
                Button(onClick = { showSignUpDialog = false; onNavigateToAuth() }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1CB0F6))) {
                    Text("Create Profile", fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSignUpDialog = false; onBackClick() }) { Text("Maybe Later", color = Color.Gray) }
            },
            containerColor = MaterialTheme.colorScheme.surface
        )
    }

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { innerPadding ->
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
                        val totalPossible = state.totalQuestions * 10
                        val percentage = if (totalPossible > 0) (state.score.toFloat() / totalPossible) * 100 else 0f
                        val passed = percentage >= 70 || state.score > 0

                        LessonCompletedView(score = state.score, passed = passed, onContinueClick = { viewModel.onLessonFinished() })
                    } else {
                        // --- DÜZELTME BURADA ---
                        // onMatchingCompleted parametresi eklendi
                        QuizContent(
                            state = state,
                            onOptionSelected = viewModel::onOptionSelected,
                            onCheckAnswer = viewModel::onCheckAnswer,
                            onNextQuestion = viewModel::onNextQuestion,
                            onCloseClick = { showQuitDialog = true },
                            onMatchingCompleted = viewModel::onMatchingCompleted // BURASI EKLENDİ
                        )

                        AnimatedVisibility(
                            visible = state.showComboAnim,
                            enter = scaleIn() + fadeIn(),
                            exit = scaleOut() + fadeOut(),
                            modifier = Modifier.align(Alignment.Center)
                        ) {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFC107)),
                                shape = RoundedCornerShape(16.dp),
                                elevation = CardDefaults.cardElevation(8.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text("🔥", fontSize = 48.sp)
                                    Text(
                                        "COMBO x${state.comboStreak}!",
                                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black),
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}