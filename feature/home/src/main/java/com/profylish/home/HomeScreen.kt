package com.profylish.home

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.profylish.home.roadmap.components.RoadmapContent
// import com.profylish.core.ui.components.StatsTopBar // Eğer bu yoksa aşağıdaki geçici olanı kullanır

@Composable
fun HomeScreen(
    onLessonClick: (String) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            StatsTopBar(
                hearts = uiState.hearts,
                gems = uiState.gems,
                streak = uiState.streak
            )
        }
    ) { innerPadding ->
        RoadmapContent(
            nodes = uiState.nodes,
            onNodeClick = onLessonClick,
            modifier = Modifier.padding(innerPadding)
        )
    }
}

// Eğer Core modülünde bu yoksa geçici olarak bunu kullan:
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsTopBar(hearts: Int, gems: Int, streak: Int) {
    TopAppBar(
        title = { Text("❤️ $hearts   💎 $gems   🔥 $streak") }
    )
}