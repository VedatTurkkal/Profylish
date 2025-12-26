    package com.profylish.profile

    import androidx.compose.foundation.BorderStroke
    import androidx.compose.foundation.background
    import androidx.compose.foundation.layout.*
    import androidx.compose.foundation.shape.CircleShape
    import androidx.compose.foundation.shape.RoundedCornerShape
    import androidx.compose.material3.*
    import androidx.compose.runtime.Composable
    import androidx.compose.runtime.getValue
    import androidx.compose.ui.Alignment
    import androidx.compose.ui.Modifier
    import androidx.compose.ui.draw.clip
    import androidx.compose.ui.graphics.Color
    import androidx.compose.ui.text.font.FontWeight
    import androidx.compose.ui.text.style.TextAlign
    import androidx.compose.ui.unit.dp
    import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
    import androidx.lifecycle.compose.collectAsStateWithLifecycle
    import com.profylish.model.user.CourseProgress

    @Composable
    fun ProfileScreen(
        onNavigateToAuth: () -> Unit,
        viewModel: ProfileViewModel = hiltViewModel()
    ) {
        val state by viewModel.uiState.collectAsStateWithLifecycle()
        val userPreferences = state.userPreferences

        val currentProfession = userPreferences.activeCourseId ?: "New Skill"

        val activeProgress = userPreferences.courses[userPreferences.activeCourseId] ?: CourseProgress()
        val currentXp = activeProgress.xp

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF58CC02)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (state.isLoggedIn) "M" else "G", // Member / Guest baş harfi
                    style = MaterialTheme.typography.displayMedium,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = if (state.isLoggedIn) "Member" else "Guest Learner",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
            )
            Text(
                text = "Learning $currentProfession",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Statistics",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                StatCard(
                    icon = "🔥",
                    value = userPreferences.streak.toString(),
                    label = "Day Streak",
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(16.dp))
                StatCard(
                    icon = "⚡",
                    value = currentXp.toString(), // DÜZELTİLDİ: Hesaplanan XP kullanılıyor
                    label = "Total XP",
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            if (!state.isLoggedIn) {
                Button(
                    onClick = onNavigateToAuth,
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1CB0F6))
                ) {
                    Text("SIGN IN / CREATE PROFILE", fontWeight = FontWeight.Bold)
                }

                Text(
                    text = "Sign in to save your progress permanently.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 16.dp),
                    textAlign = TextAlign.Center
                )
            } else {
                OutlinedButton(
                    onClick = { viewModel.signOut() },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("SIGN OUT", fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    @Composable
    fun StatCard(icon: String, value: String, label: String, modifier: Modifier = Modifier) {
        Card(
            modifier = modifier,
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(2.dp, Color(0xFFE5E5E5)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = icon, style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = value, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                }
                Text(text = label, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
            }
        }
    }