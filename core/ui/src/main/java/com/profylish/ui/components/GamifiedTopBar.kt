package com.profylish.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import androidx.compose.ui.unit.min
import androidx.compose.ui.unit.sp
import com.profylish.ui.R

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
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .height(72.dp),
        shadowElevation = 6.dp,
        color = MaterialTheme.colorScheme.primary,
        shape = RoundedCornerShape(24.dp)
    ) {
        Column (
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.Start
        ) {
            Box {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clickable { expanded = true }
                            .padding(8.dp)
                            .width(50.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = android.R.drawable.ic_menu_myplaces),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))
                    StatsTopBar(heartCount, gemCount, streakCount)
                }

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.background(Color(0xFF222222))
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
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun GamifiedTopBarPreview(){
    GamifiedTopBar("Software Engineer", 500, 2, 5)
}