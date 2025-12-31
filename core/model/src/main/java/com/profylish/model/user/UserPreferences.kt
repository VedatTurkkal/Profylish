package com.profylish.model.user

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CourseProgress(
    val level: Int = 1,
    @SerialName("stages_completed") val stagesCompleted: Int = 0,
    val xp: Int = 0,
    @SerialName("is_first_lesson_done") val isFirstLessonDone: Boolean = false,
    @SerialName("last_played") val lastPlayed: Long = 0
)

@Serializable
data class UserPreferences(
    @SerialName("id") val userId: String? = null,
    val username: String? = "Guest Learner",
    val email: String? = null,

    @SerialName("avatar_url") val avatarUrl: String? = null,

    @SerialName("active_course_id") val activeCourseId: String? = null,
    @SerialName("experience_level") val experienceLevel: String = "I'm Starting from Scratch",

    @SerialName("xp") val totalXp: Int = 0,
    @SerialName("last_lesson_date") val lastLessonDate: String? = null,

    val courses: Map<String, CourseProgress> = emptyMap(),

    @SerialName("completed_categories")
    val completedCategories: Map<Int, Set<String>> = emptyMap(),

    val gems: Int = 500,
    val hearts: Int = 5,
    val streak: Int = 0,
    @SerialName("has_streak_freeze") val hasStreakFreeze: Boolean = false,

    @SerialName("is_vibration_enabled") val isVibrationEnabled: Boolean = true,
    @SerialName("is_dark_mode_enabled") val isDarkModeEnabled: Boolean = false,
    @SerialName("is_notifications_enabled") val isNotificationsEnabled: Boolean = true
)