package com.profylish.model.roadmap

import androidx.compose.runtime.Immutable

@Immutable
data class RoadmapNode(
    val id: String,
    val title: String,
    val status: NodeStatus,
    val type: NodeType = NodeType.LESSON,
    val stars: Int = 0
)

enum class NodeStatus {
    LOCKED,
    ACTIVE,
    COMPLETED
}

enum class NodeType {
    LESSON,
    CHEST,
    MILESTONE
}