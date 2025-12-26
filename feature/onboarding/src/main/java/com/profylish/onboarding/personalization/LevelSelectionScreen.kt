package com.profylish.onboarding.personalization

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
// DÜZELTME 1: Deprecated uyarısı için doğru import
import androidx.hilt.navigation.compose.hiltViewModel
import com.profylish.onboarding.OnboardingViewModel

@Composable
fun LevelSelectionScreen(
    occupationId: String,
    occupationGroup: String,
    onOnboardingFinished: (String) -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = "How much experience do you have in this field?",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        val levels = listOf(
            "I'm Starting from Scratch" to "I know nothing about this field.",
            "I Have Some Knowledge" to "I know the basic concepts.",
            "I'm Experienced" to "I have worked in this field before."
        )

        levels.forEach { (title, subtitle) ->
            LevelCard(
                title = title,
                subtitle = subtitle,
                onClick = {
                    viewModel.saveUserPreference(jobTitle = occupationId)

                    onOnboardingFinished(occupationId)
                }
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun LevelCard(title: String, subtitle: String, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(2.dp, MaterialTheme.colorScheme.outlineVariant),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(text = subtitle, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
            }
        }
    }
}