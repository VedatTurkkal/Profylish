package com.profylish.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBackClick: () -> Unit,
    onSignOutSuccess: () -> Unit, // Çıkış yapınca ne olsun?
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val prefs by viewModel.userPreferences.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp)
        ) {
            // --- GENERAL SECTION ---
            Text("General", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(8.dp))

            // Dark Mode
            SettingsSwitchItem(
                title = "Dark Mode",
                description = "Switch between light and dark themes",
                isChecked = prefs.isDarkModeEnabled,
                onCheckedChange = { isChecked ->
                    viewModel.updateSettings(prefs.isVibrationEnabled, isChecked, prefs.isNotificationsEnabled)
                }
            )

            // Vibration
            SettingsSwitchItem(
                title = "Haptic Feedback",
                description = "Vibrate on correct/incorrect answers",
                isChecked = prefs.isVibrationEnabled,
                onCheckedChange = { isChecked ->
                    viewModel.updateSettings(isChecked, prefs.isDarkModeEnabled, prefs.isNotificationsEnabled)
                }
            )

            // Notifications
            SettingsSwitchItem(
                title = "Notifications",
                description = "Daily practice reminders",
                isChecked = prefs.isNotificationsEnabled,
                onCheckedChange = { isChecked ->
                    viewModel.updateSettings(prefs.isVibrationEnabled, prefs.isDarkModeEnabled, isChecked)
                }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

            // --- ACCOUNT SECTION ---
            Text("Account", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(8.dp))

            if (viewModel.isLoggedIn) {
                Button(
                    onClick = {
                        viewModel.signOut()
                        onSignOutSuccess()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Sign Out", fontWeight = FontWeight.Bold)
                }
            } else {
                Text("You are currently using a Guest account.", color = Color.Gray)
            }
        }
    }
}

@Composable
fun SettingsSwitchItem(
    title: String,
    description: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
            Text(text = description, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        }
        Switch(
            checked = isChecked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Color(0xFF58CC02))
        )
    }
}