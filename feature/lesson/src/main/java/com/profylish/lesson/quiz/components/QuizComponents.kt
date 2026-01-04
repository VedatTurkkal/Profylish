package com.profylish.lesson.quiz.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

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
        else -> MaterialTheme.colorScheme.outlineVariant
    }
    val backgroundColor = when {
        isAnswerChecked && isCorrectAnswer -> Color(0xFFD7FFB8)
        isAnswerChecked && isSelected -> Color(0xFFFFDFE0)
        isSelected -> Color(0xFFDDF4FF)
        else -> MaterialTheme.colorScheme.surface
    }
    val textColor = if (!isAnswerChecked && !isSelected) MaterialTheme.colorScheme.onSurface else Color(0xFF4B4B4B)

    Surface(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).clickable(enabled = !isAnswerChecked, onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(2.dp, borderColor),
        color = backgroundColor
    ) {
        Text(text, modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium), color = textColor)
    }
}

@Composable
fun MatchingCard(
    text: String,
    isSelected: Boolean,
    isMatched: Boolean,
    isSmallText: Boolean = false,
    onClick: () -> Unit
) {
    val bgColor = when {
        isMatched -> Color.Transparent
        isSelected -> Color(0xFFDDF4FF)
        else -> MaterialTheme.colorScheme.surface
    }

    val borderColor = if (isSelected) Color(0xFF1CB0F6) else MaterialTheme.colorScheme.outlineVariant
    val textColor = if (isMatched) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f) else MaterialTheme.colorScheme.onSurface

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(if (isSmallText) 100.dp else 60.dp)
            .clickable(enabled = !isMatched && !isSelected) { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        border = if (!isMatched) BorderStroke(2.dp, borderColor) else null
    ) {
        Box(modifier = Modifier.fillMaxSize().padding(8.dp), contentAlignment = Alignment.Center) {
            Text(
                text = text,
                style = if (isSmallText) MaterialTheme.typography.bodySmall else MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = textColor,
                textAlign = TextAlign.Center,
                maxLines = if (isSmallText) 4 else 2
            )
        }
    }
}

@Composable
fun TrueFalseCard(
    text: String,
    baseColor: Color,
    isSelected: Boolean,
    isAnswerChecked: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (isSelected) baseColor else MaterialTheme.colorScheme.outlineVariant
    val bgColor = if (isSelected) baseColor.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surface
    val textColor = if (isSelected) baseColor else MaterialTheme.colorScheme.onSurface

    Surface(
        modifier = Modifier.fillMaxWidth().height(100.dp).clip(RoundedCornerShape(16.dp)).clickable(enabled = !isAnswerChecked, onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(2.dp, borderColor),
        color = bgColor
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(text, style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold), color = textColor)
        }
    }
}