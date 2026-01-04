package com.profylish.lesson.quiz.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.profylish.lesson.model.QuizUiState
import com.profylish.model.lesson.QuestionType
import com.profylish.ui.components.RavenMascot
import com.profylish.ui.components.RavenState

@Composable
fun QuizContent(
    state: QuizUiState.Success,
    onOptionSelected: (Int) -> Unit,
    onCheckAnswer: () -> Unit,
    onNextQuestion: () -> Unit,
    onCloseClick: () -> Unit,
    // HATA DÜZELTİLDİ: Varsayılan değer (viewModel::...) kaldırıldı.
    onMatchingCompleted: () -> Unit
) {
    val progress by animateFloatAsState(
        targetValue = (state.currentQuestionIndex + 1) / state.totalQuestions.toFloat(),
        label = "ProgressBar"
    )

    Column(modifier = Modifier.fillMaxSize()) {

        // TOP BAR
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onCloseClick) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            }
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.weight(1f).height(12.dp).clip(RoundedCornerShape(8.dp)),
                color = Color(0xFF58CC02),
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
            Spacer(modifier = Modifier.width(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Favorite, contentDescription = "Hearts", tint = Color(0xFFFF4B4B))
                Spacer(modifier = Modifier.width(4.dp))
                Text("${state.hearts}", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = Color(0xFFFF4B4B))
            }
        }

        // CONTENT
        Column(modifier = Modifier.weight(1f)) {

            // Soru ve Raven (Eşleştirme hariç)
            if (state.currentQuestion.type != QuestionType.MATCHING_PAIRS) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    Box(modifier = Modifier.size(80.dp)) {
                        RavenMascot(
                            state = remember(state.isAnswerChecked, state.isAnswerCorrect) {
                                if (state.isAnswerChecked && !state.isAnswerCorrect) RavenState.CONFUSED
                                else if (state.isAnswerChecked && state.isAnswerCorrect) RavenState.CORRECT
                                else RavenState.THINKING
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))

                    val bubbleShape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomEnd = 16.dp, bottomStart = 4.dp)
                    Surface(
                        shape = bubbleShape,
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.weight(1f).border(1.dp, MaterialTheme.colorScheme.outlineVariant, bubbleShape)
                    ) {
                        Text(
                            text = state.currentQuestion.questionText,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.padding(16.dp),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            } else {
                Text(
                    text = state.currentQuestion.questionText,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            // Seçenekler
            Column(
                modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.Center
            ) {
                when (state.currentQuestion.type) {
                    QuestionType.MATCHING_PAIRS -> {
                        Box(modifier = Modifier.fillMaxWidth()) {
                            MatchingQuizContent(
                                pairs = state.currentQuestion.matchingPairs ?: emptyList(),
                                onAllMatched = onMatchingCompleted
                            )
                        }
                    }
                    QuestionType.FILL_IN_THE_BLANK -> {
                        state.currentQuestion.options.forEachIndexed { index, optionText ->
                            QuizOptionCard(
                                text = optionText,
                                isSelected = state.selectedOptionIndex == index,
                                isAnswerChecked = state.isAnswerChecked,
                                isCorrectAnswer = index == state.currentQuestion.correctAnswerIndex,
                                onClick = { onOptionSelected(index) }
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                    QuestionType.TRUE_FALSE -> {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            state.currentQuestion.options.forEachIndexed { index, optionText ->
                                val color = if(optionText == "True") Color(0xFF58CC02) else Color(0xFFEA2B2B)
                                Box(modifier = Modifier.weight(1f)) {
                                    TrueFalseCard(
                                        text = optionText,
                                        baseColor = color,
                                        isSelected = state.selectedOptionIndex == index,
                                        isAnswerChecked = state.isAnswerChecked,
                                        onClick = { onOptionSelected(index) }
                                    )
                                }
                            }
                        }
                    }
                    else -> {
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
                }
            }
        }

        // FOOTER
        if (state.currentQuestion.type != QuestionType.MATCHING_PAIRS) {
            val footerColor = if (state.isAnswerChecked) {
                if (state.isAnswerCorrect) Color(0xFFD7FFB8) else Color(0xFFFFDFE0)
            } else {
                MaterialTheme.colorScheme.background
            }

            Column(modifier = Modifier.fillMaxWidth().background(footerColor).padding(16.dp)) {
                if (state.isAnswerChecked) {
                    Row(modifier = Modifier.padding(bottom = 16.dp)) {
                        val icon = if (state.isAnswerCorrect) "🎉" else "❌"
                        val title = if (state.isAnswerCorrect) "Excellent!" else "Correct answer:"
                        Text(text = icon, style = MaterialTheme.typography.headlineSmall)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                title,
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = if (state.isAnswerCorrect) Color(0xFF58CC02) else Color(0xFFEA2B2B)
                            )
                            if (!state.isAnswerCorrect && state.currentQuestion.options.isNotEmpty()) {
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
                        disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = if (state.isAnswerChecked) "CONTINUE" else "CHECK",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                }
            }
        }
    }
}