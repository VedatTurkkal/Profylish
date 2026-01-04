package com.profylish.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.profylish.ui.components.RavenMascot
import com.profylish.ui.components.RavenState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LessonSelectionBottomSheet(
    clickedLevelId: Int,
    userLevel: Int,
    completedCategories: Set<String> = emptySet(),
    onDismiss: () -> Unit,
    onCategorySelected: (String) -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            RavenMascot(
                state = RavenState.TEACHER, // veya ROADMAP
                modifier = Modifier.size(100.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Level $clickedLevelId",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = Color(0xFF58CC02),
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                text = "Complete all sections to unlock the next level!",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            val categories = listOf(
                "TERM" to "Terminology",
                "IDIOM" to "Idioms",
                "PHRASAL_VERB" to "Phrasal Verbs",
                "ACRONYM" to "Acronyms"
            )

            categories.forEach { (key, title) ->
                val isCompleted = completedCategories.contains(key)

                CategoryItem(
                    title = title,
                    isCompleted = isCompleted,
                    onClick = {
                        onCategorySelected(key)
                        onDismiss()
                    }
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
fun CategoryItem(
    title: String,
    isCompleted: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (isCompleted) Color(0xFFFFC107) else Color(0xFFE5E5E5)
    val iconColor = if (isCompleted) Color(0xFFFFC107) else Color(0xFF58CC02)
    val icon = if (isCompleted) Icons.Default.Check else Icons.Default.PlayArrow
    val backgroundColor = if (isCompleted) Color(0xFFFFF8E1) else Color.White

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .border(2.dp, borderColor, RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(iconColor.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = iconColor)
        }

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = Color.Black,
            modifier = Modifier.weight(1f)
        )

        if (isCompleted) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "DONE",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFFFFC107)
                )
                RavenMascot(state = RavenState.ACHIEVEMENT, modifier = Modifier.size(24.dp))
            }
        }
    }
}