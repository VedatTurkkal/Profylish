package com.profylish.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun GamifiedTopBar(
    professionName: String,
    gemCount: Int,
    streakCount: Int,
    heartCount: Int,
    availableCourses: List<String> = emptyList(),
    onSwitchCourse: (String) -> Unit = {},
    onAddCourse: () -> Unit = {}
) {
    var expanded by remember { mutableStateOf(false) }

    Surface(
        shadowElevation = 4.dp,
        color = Color(0xFF141414)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // --- SOL TARAF: Meslek Seçici (Dropdown) ---
            Box {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clickable { expanded = true }
                        .padding(4.dp)
                ) {
                    Icon(
                        painter = painterResource(id = android.R.drawable.ic_menu_myplaces),
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = professionName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "Switch Course",
                        tint = Color.Gray
                    )
                }

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    containerColor = Color(0xFF222222)
                ) {
                    availableCourses.forEach { course ->
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = course,
                                        color = if (course == professionName) Color(0xFF58CC02) else Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                    if (course == professionName) {
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Icon(Icons.Default.Check, null, tint = Color(0xFF58CC02))
                                    }
                                }
                            },
                            onClick = {
                                onSwitchCourse(course)
                                expanded = false
                            }
                        )
                    }

                    if (availableCourses.size < 3) {
                        HorizontalDivider(color = Color.Gray)
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Add, null, tint = Color.Gray)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Add New Course", color = Color.Gray)
                                }
                            },
                            onClick = {
                                onAddCourse()
                                expanded = false
                            }
                        )
                    }
                }
            }

            // --- SAĞ TARAF: İstatistikler ---
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Gems
                Icon(
                    painter = painterResource(android.R.drawable.ic_dialog_email),
                    contentDescription = null,
                    tint = Color(0xFF1CB0F6),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = gemCount.toString(), color = Color(0xFF1CB0F6), fontWeight = FontWeight.Bold)

                Spacer(modifier = Modifier.width(16.dp))

                // Streak
                Icon(
                    painter = painterResource(android.R.drawable.ic_lock_idle_charging),
                    contentDescription = null,
                    tint = Color(0xFFFF9600),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = streakCount.toString(), color = Color(0xFFFF9600), fontWeight = FontWeight.Bold)

                Spacer(modifier = Modifier.width(16.dp))

                // Hearts
                Icon(
                    painter = painterResource(android.R.drawable.stat_notify_error),
                    contentDescription = null,
                    tint = if (heartCount == 0) Color.Gray else Color(0xFFFF4B4B),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = heartCount.toString(), color = if (heartCount == 0) Color.Gray else Color(0xFFFF4B4B), fontWeight = FontWeight.Bold)
            }
        }
    }
}