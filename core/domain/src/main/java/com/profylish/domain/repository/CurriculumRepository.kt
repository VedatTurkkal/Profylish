package com.profylish.domain.repository

import com.profylish.model.curriculum.LearningUnit

interface CurriculumRepository {
    suspend fun getCurriculumForOccupation(occupationGroup: String): List<LearningUnit>
}