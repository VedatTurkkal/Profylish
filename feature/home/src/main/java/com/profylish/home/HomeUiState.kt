package com.profylish.home

import com.profylish.model.roadmap.RoadmapNode

data class HomeUiState(
    val isLoading: Boolean = false,
    val profession: String = "",
    val availableCourses: List<String> = emptyList(),
    val level: Int = 1,
    val currentStage: Int = 0,
    val maxStages: Int = 3,
    val gems: Int = 0,
    val hearts: Int = 0,
    val streak: Int = 0,
    val nodes: List<RoadmapNode> = emptyList(),
    val isVibrationEnabled: Boolean = true
)