package com.profylish.home

import androidx.compose.runtime.Immutable
import com.profylish.model.roadmap.RoadmapNode

@Immutable
data class HomeUiState(
    val isLoading: Boolean = false,
    val level: Int = 1,
    val gems: Int = 0,
    val hearts: Int = 5,
    val streak: Int = 0,
    val nodes: List<RoadmapNode> = emptyList()
)