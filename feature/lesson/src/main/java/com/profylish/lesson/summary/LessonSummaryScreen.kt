package com.profylish.lesson.summary

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.profylish.ui.components.RavenMascot
import com.profylish.ui.components.RavenState

@Composable
fun LessonCompletedView(score: Int, passed: Boolean, onContinueClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val mascotState = if (passed) RavenState.LESSON_FINISH else RavenState.CONFUSED
        val title = if (passed) "Lesson Complete!" else "Don't give up!"
        val btnColor = if (passed) Color(0xFF58CC02) else Color.Gray

        RavenMascot(
            state = mascotState,
            modifier = Modifier.size(200.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            color = if (passed) Color(0xFF58CC02) else MaterialTheme.colorScheme.error
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = if (passed) "You earned $score XP" else "Review the lesson and try again.",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onContinueClick,
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = btnColor)
        ) {
            Text("CONTINUE", color = Color.White)
        }
    }
}