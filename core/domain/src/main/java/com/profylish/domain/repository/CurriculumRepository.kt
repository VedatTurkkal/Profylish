package com.profylish.domain.repository

import com.profylish.model.curriculum.LearningUnit
import com.profylish.model.roadmap.RoadmapNode

interface CurriculumRepository {

    suspend fun getCurriculumForOccupation(occupationGroup: String): List<LearningUnit>

    suspend fun generateRoadmap(occupationTitle: String, currentLevel: Int): List<RoadmapNode>
}