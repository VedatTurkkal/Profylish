package com.profylish.home

import com.profylish.model.roadmap.RoadmapNode

data class HomeUiState(
    val isLoading: Boolean = false,
    val profession: String = "",
    val availableCourses: List<String> = emptyList(),
    val level: Int = 1,
    val gems: Int = 0,
    val hearts: Int = 5,
    val nodes: List<RoadmapNode> = emptyList()
)