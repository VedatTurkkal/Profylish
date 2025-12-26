package com.profylish.model.user

import kotlinx.serialization.Serializable

@Serializable
data class CourseProgress(
    val level: Int = 1,
    val xp: Int = 0,
    val isFirstLessonDone: Boolean = false,
    val lastPlayed: Long = 0
)


@Serializable
data class UserPreferences(
    val activeCourseId: String? = null,

    val courses: Map<String, CourseProgress> = emptyMap(),

    val gems: Int = 0,
    val hearts: Int = 5,
    val streak: Int = 0,
    val userId: String? = null
)