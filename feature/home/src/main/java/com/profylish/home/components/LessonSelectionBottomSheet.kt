package com.profylish.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LessonSelectionBottomSheet(
    clickedLevelId: Int,
    userLevel: Int,
    userStage: Int,
    onDismiss: () -> Unit,
    onLessonSelected: (Int) -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 48.dp)
        ) {
            Text(
                text = "Level $clickedLevelId",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                text = "Complete all 3 stages to advance!",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            val totalLessons = 3

            for (lessonIndex in 0 until totalLessons) {
                val state = getLessonState(
                    lessonIndex = lessonIndex,
                    clickedLevel = clickedLevelId,
                    userLevel = userLevel,
                    userStage = userStage
                )

                LessonItem(
                    index = lessonIndex + 1,
                    state = state,
                    onClick = {
                        // Tıklanan dersin indeksini (0, 1, 2) yukarı gönderiyoruz
                        if (state == LessonState.ACTIVE || state == LessonState.COMPLETED) {
                            onLessonSelected(lessonIndex)
                            onDismiss()
                        }
                    }
                )

                if (lessonIndex < totalLessons - 1) {
                    Box(
                        modifier = Modifier
                            .padding(start = 24.dp)
                            .width(2.dp)
                            .height(16.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    )
                }
            }
        }
    }
}

enum class LessonState { LOCKED, ACTIVE, COMPLETED }

fun getLessonState(lessonIndex: Int, clickedLevel: Int, userLevel: Int, userStage: Int): LessonState {
    return when {
        clickedLevel < userLevel -> LessonState.COMPLETED
        clickedLevel > userLevel -> LessonState.LOCKED
        else -> {
            when {
                lessonIndex < userStage -> LessonState.COMPLETED
                lessonIndex == userStage -> LessonState.ACTIVE
                else -> LessonState.LOCKED
            }
        }
    }
}

@Composable
fun LessonItem(
    index: Int,
    state: LessonState,
    onClick: () -> Unit
) {
    val backgroundColor = when (state) {
        LessonState.ACTIVE -> MaterialTheme.colorScheme.primaryContainer
        LessonState.COMPLETED -> MaterialTheme.colorScheme.secondaryContainer
        LessonState.LOCKED -> MaterialTheme.colorScheme.surfaceVariant
    }

    val icon: ImageVector = when (state) {
        LessonState.ACTIVE -> Icons.Default.PlayArrow
        LessonState.COMPLETED -> Icons.Default.Check
        LessonState.LOCKED -> Icons.Default.Lock
    }

    val contentColor = when(state) {
        LessonState.LOCKED -> Color.Gray
        else -> MaterialTheme.colorScheme.onSurface
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor.copy(alpha = 0.5f))
            .clickable(enabled = state != LessonState.LOCKED) { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(
                    if (state == LessonState.ACTIVE) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.surface
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (state == LessonState.ACTIVE) Color.White else contentColor
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column {
            Text(
                text = "Lesson $index",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = contentColor
            )
            Text(
                text = when(state) {
                    LessonState.COMPLETED -> "Review"
                    LessonState.ACTIVE -> "Start"
                    LessonState.LOCKED -> "Locked"
                },
                style = MaterialTheme.typography.bodySmall,
                color = contentColor.copy(alpha = 0.7f)
            )
        }
    }
}