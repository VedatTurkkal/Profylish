package com.profylish.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.profylish.home.roadmap.components.RoadmapContent
import com.profylish.ui.components.GamifiedTopBar
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    onLessonClick: (String, String) -> Unit,
    onNavigateToSearch: () -> Unit, // Yeni kurs ekleme rotası
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            GamifiedTopBar(
                professionName = uiState.profession,
                gemCount = uiState.gems,
                streakCount = 0,
                heartCount = uiState.hearts,
                availableCourses = uiState.availableCourses,
                onSwitchCourse = { viewModel.switchCourse(it) },
                onAddCourse = onNavigateToSearch
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            RoadmapContent(
                nodes = uiState.nodes,
                onNodeClick = { levelId ->
                    // Kalp kontrolü burada yapılıyor
                    if (uiState.hearts > 0) {
                        onLessonClick(levelId, uiState.profession)
                    } else {
                        scope.launch {
                            snackbarHostState.showSnackbar("You have no hearts left! Practice to earn or wait.")
                        }
                    }
                }
            )
        }
    }
}