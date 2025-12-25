package com.profylish.lesson.quiz

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.profylish.lesson.model.QuizUiState

@Composable
fun QuizScreen(
    onBackClick: () -> Unit,
    viewModel: QuizViewModel = hiltViewModel()
) {
    // ViewModel'deki UI State'i dinliyoruz
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // DİKKAT: LaunchedEffect BURADAN SİLİNDİ.
    // ViewModel artık veriyi init bloğunda kendisi yüklüyor.

    Scaffold(
        containerColor = Color.White
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (val state = uiState) {
                is QuizUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                is QuizUiState.Error -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "⚠️ ${state.message}",
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(16.dp)
                        )
                        Button(onClick = onBackClick) { Text("Go Back") }
                    }
                }

                is QuizUiState.Success -> {
                    if (state.isLessonCompleted) {
                        LessonCompletedView(score = state.score, onBackClick = onBackClick)
                    } else {
                        QuizContent(
                            state = state,
                            onOptionSelected = viewModel::onOptionSelected, // Kısa yazım (Method Reference)
                            onCheckAnswer = viewModel::onCheckAnswer,
                            onNextQuestion = viewModel::onNextQuestion,
                            onCloseClick = onBackClick
                        )
                    }
                }
            }
        }
    }
}

// ... QuizContent, QuizOptionCard ve LessonCompletedView kodları AYNI kalacak ...
// (Tekrar kopyalamadım, mevcut kodunuzdaki alt kısımlar geçerli)
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
        // HEADER
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

        // BODY
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

        // FOOTER
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
fun LessonCompletedView(score: Int, onBackClick: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "🎉", style = MaterialTheme.typography.displayLarge)
        Spacer(modifier = Modifier.height(16.dp))
        Text("Lesson Complete!", style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold), color = Color(0xFF58CC02))
        Spacer(modifier = Modifier.height(8.dp))
        Text("You earned $score XP", style = MaterialTheme.typography.titleLarge, color = Color(0xFFFFC800))
        Spacer(modifier = Modifier.height(32.dp))
        Button(onClick = onBackClick, modifier = Modifier.fillMaxWidth(0.8f).height(50.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF58CC02))) {
            Text("CONTINUE")
        }
    }
}