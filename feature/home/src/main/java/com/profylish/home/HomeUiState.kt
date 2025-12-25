package com.profylish.home

import com.profylish.model.roadmap.RoadmapNode

data class HomeUiState(
    val profession: String = "", // <-- EKLENDİ: Seçilen mesleği burada tutacağız
    val level: Int = 1,
    val gems: Int = 0,
    val hearts: Int = 5,
    val nodes: List<RoadmapNode> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)