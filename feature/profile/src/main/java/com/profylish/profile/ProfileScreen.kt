package com.profylish.profile

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest

@Composable
fun ProfileScreen(
    onNavigateToAuth: () -> Unit,
    onNavigateToSettings: () -> Unit, // Settings ekranına gitmek için
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val userPreferences = state.userPreferences
    val context = LocalContext.current

    // Fotoğraf Seçici
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            if (uri != null) viewModel.onAvatarSelected(context, uri)
        }
    )

    var showEditNameDialog by remember { mutableStateOf(false) }
    var showAvatarOptionsDialog by remember { mutableStateOf(false) } // Fotoğraf seçenekleri
    var newUsername by remember { mutableStateOf("") }

    val displayUsername = if (state.isLoggedIn) userPreferences.username ?: "Learner" else "Guest Learner"
    val userInitial = displayUsername.firstOrNull()?.toString()?.uppercase() ?: "G"

    // --- FOTOĞRAF SEÇENEKLERİ DİYALOĞU ---
    if (showAvatarOptionsDialog) {
        AlertDialog(
            onDismissRequest = { showAvatarOptionsDialog = false },
            title = { Text("Profile Picture") },
            text = {
                Column {
                    ListItem(
                        headlineContent = { Text("Choose from Gallery") },
                        leadingContent = { Icon(Icons.Outlined.Image, contentDescription = null) },
                        modifier = Modifier.clickable {
                            showAvatarOptionsDialog = false
                            photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        }
                    )
                    if (userPreferences.avatarUrl != null) {
                        ListItem(
                            headlineContent = { Text("Remove Photo", color = MaterialTheme.colorScheme.error) },
                            leadingContent = { Icon(Icons.Outlined.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                            modifier = Modifier.clickable {
                                showAvatarOptionsDialog = false
                                viewModel.deleteAvatar()
                            }
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showAvatarOptionsDialog = false }) { Text("Cancel") }
            }
        )
    }

    // --- İSİM DEĞİŞTİRME DİYALOĞU ---
    if (showEditNameDialog) {
        AlertDialog(
            onDismissRequest = { showEditNameDialog = false },
            title = { Text("Edit Username") },
            text = {
                OutlinedTextField(
                    value = newUsername,
                    onValueChange = { newUsername = it },
                    singleLine = true
                )
            },
            confirmButton = {
                Button(onClick = {
                    if (newUsername.isNotBlank()) viewModel.updateUsername(newUsername)
                    showEditNameDialog = false
                }) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { showEditNameDialog = false }) { Text("Cancel") }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        // --- ÜST BAR (SETTINGS BUTONU) ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            IconButton(
                onClick = onNavigateToSettings,
                modifier = Modifier.align(Alignment.CenterEnd)
            ) {
                Icon(Icons.Default.Settings, contentDescription = "Settings", tint = Color.Gray)
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // --- AVATAR ---
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF58CC02))
                    .clickable {
                        if (state.isLoggedIn) {
                            showAvatarOptionsDialog = true // Seçenekleri aç
                        } else {
                            onNavigateToAuth()
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                if (userPreferences.avatarUrl != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(context).data(userPreferences.avatarUrl).crossfade(true).build(),
                        contentDescription = "Profile Picture",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Text(text = userInitial, style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Bold), color = Color.White)
                }

                if (state.isLoggedIn) {
                    Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.1f)))
                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color.White, modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp).size(24.dp))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- İSİM ---
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(enabled = state.isLoggedIn) {
                        newUsername = displayUsername
                        showEditNameDialog = true
                    }
                    .padding(8.dp)
            ) {
                Text(text = displayUsername, style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold))
                if (state.isLoggedIn) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color.Gray, modifier = Modifier.size(20.dp))
                }
            }

            Text(text = "Learning ${userPreferences.activeCourseId ?: "New Skill"}", style = MaterialTheme.typography.bodyLarge, color = Color.Gray)

            Spacer(modifier = Modifier.height(32.dp))

            // --- İSTATİSTİKLER ---
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                StatCard(icon = "🔥", value = userPreferences.streak.toString(), label = "Day Streak", modifier = Modifier.weight(1f))
                Spacer(modifier = Modifier.width(16.dp))
                StatCard(icon = "⚡", value = userPreferences.courses.values.sumOf { it.xp }.toString(), label = "Total XP", modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.weight(1f))

            if (!state.isLoggedIn) {
                Button(
                    onClick = onNavigateToAuth,
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1CB0F6))
                ) { Text("SIGN IN / CREATE PROFILE", fontWeight = FontWeight.Bold) }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun StatCard(icon: String, value: String, label: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        border = BorderStroke(2.dp, MaterialTheme.colorScheme.outlineVariant), // Temaya uygun border
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