package com.profylish.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.profylish.home.roadmap.components.RoadmapContent

@Composable
fun HomeScreen(
    onLessonClick: (String) -> Unit, // Sadece ID gönderiyor
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Box(modifier = Modifier.fillMaxSize()) {
        RoadmapContent(
            nodes = uiState.nodes,
            onNodeClick = { levelId ->
                onLessonClick(levelId)
            }
        )
    }
}