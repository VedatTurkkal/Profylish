package com.profylish.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.profylish.home.components.LessonSelectionBottomSheet
import com.profylish.home.roadmap.components.RoadmapContent
import com.profylish.ui.components.GamifiedTopBar
import kotlinx.coroutines.launch
import androidx.compose.ui.res.painterResource

@Composable
fun HomeScreen(
    onLessonClick: (String, String, Boolean) -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToChat: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var selectedLevelId by remember { mutableStateOf<String?>(null) }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            GamifiedTopBar(
                professionName = uiState.profession,
                gemCount = uiState.gems,
                streakCount = uiState.streak,
                heartCount = uiState.hearts,
                availableCourses = uiState.availableCourses,
                onSwitchCourse = { viewModel.switchCourse(it) },
                onAddCourse = onNavigateToSearch
            )
        },
        floatingActionButton = {
            androidx.compose.material3.FloatingActionButton(
                onClick = onNavigateToChat,
                containerColor = androidx.compose.material3.MaterialTheme.colorScheme.primaryContainer
            ) {
                androidx.compose.material3.Icon(
                    painter = painterResource(com.profylish.ui.R.drawable.robot), // Veya Default.Chat
                    contentDescription = "AI Interview"
                )
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            RoadmapContent(
                nodes = uiState.nodes,
                onNodeClick = { levelId ->
                    val clickedLevelInt = levelId.toIntOrNull() ?: 1
                    if (clickedLevelInt <= uiState.level) {
                        selectedLevelId = levelId
                    } else {
                        scope.launch {
                            snackbarHostState.showSnackbar("This level is locked. Complete previous levels first!")
                        }
                    }
                }
            )
        }
    }

    if (selectedLevelId != null) {
        val clickedLevelInt = selectedLevelId!!.toIntOrNull() ?: 1

        LessonSelectionBottomSheet(
            clickedLevelId = clickedLevelInt,
            userLevel = uiState.level,
            userStage = uiState.currentStage,
            onDismiss = { selectedLevelId = null },
            onLessonSelected = { lessonIndex ->
                if (uiState.hearts > 0) {
                    val isProgression = (clickedLevelInt == uiState.level) && (lessonIndex == uiState.currentStage)
                    onLessonClick(selectedLevelId!!, uiState.profession, isProgression)
                    selectedLevelId = null
                } else {
                    scope.launch {
                        snackbarHostState.showSnackbar("You have no hearts left! Practice to earn or wait.")
                    }
                }
            }
        )
    }
}