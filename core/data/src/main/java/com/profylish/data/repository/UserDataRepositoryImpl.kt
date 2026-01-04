@file:OptIn(InternalSerializationApi::class)

package com.profylish.data.repository

import android.os.Build
import android.util.Log
import com.profylish.datastore.UserPreferencesDataSource
import com.profylish.domain.repository.AuthRepository
import com.profylish.domain.repository.UserDataRepository
import com.profylish.model.user.CourseProgress
import com.profylish.model.user.UserPreferences
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.InternalSerializationApi
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import javax.inject.Inject

class UserDataRepositoryImpl @Inject constructor(
    private val preferencesDataSource: UserPreferencesDataSource,
    private val supabaseClient: SupabaseClient,
    private val authRepository: AuthRepository
) : UserDataRepository {

    override val userData: Flow<UserPreferences> = preferencesDataSource.userData
    private val scope = CoroutineScope(Dispatchers.IO)
    private val TAG = "UserDataRepository"

    // --- Profile & Avatar ---

    override suspend fun updateProfile(username: String?, avatarUrl: String?) {
        val userId = supabaseClient.auth.currentUserOrNull()?.id ?: return

        try {
            supabaseClient.from("profiles").update(
                {
                    if (username != null) set("username", username)
                    if (avatarUrl != null) set("avatar_url", avatarUrl)
                }
            ) { filter { eq("id", userId) } }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating cloud profile: ${e.message}")
        }

        preferencesDataSource.updateData { current ->
            var updated = current
            if (username != null) updated = updated.copy(username = username)
            if (avatarUrl != null) updated = updated.copy(avatarUrl = avatarUrl)
            updated
        }
    }

    override suspend fun uploadAvatar(byteArray: ByteArray): String {
        val userId = supabaseClient.auth.currentUserOrNull()?.id ?: throw Exception("User not found")
        val fileName = "$userId/avatar_${System.currentTimeMillis()}.jpg"
        val bucket = supabaseClient.storage.from("avatars")

        bucket.upload(fileName, byteArray, true)
        val publicUrl = bucket.publicUrl(fileName)

        updateProfile(username = null, avatarUrl = publicUrl)
        return publicUrl
    }

    override suspend fun updateUsername(newUsername: String) {
        updateProfile(username = newUsername, avatarUrl = null)
    }

    override suspend fun deleteAvatar() {
        val userId = supabaseClient.auth.currentUserOrNull()?.id ?: return
        preferencesDataSource.updateData { it.copy(avatarUrl = null) }
        try {
            supabaseClient.from("profiles").update(
                { set("avatar_url", null as String?) }
            ) { filter { eq("id", userId) } }
        } catch (e: Exception) { e.printStackTrace() }
    }

    // --- Course & Stats Logic ---

    override suspend fun switchOrAddCourse(occupationTitle: String) {
        preferencesDataSource.updateData { current ->
            val existingCourses = current.courses.toMutableMap()
            if (existingCourses.containsKey(occupationTitle)) {
                return@updateData current.copy(activeCourseId = occupationTitle)
            }
            existingCourses[occupationTitle] = CourseProgress(level = 1, xp = 0)
            current.copy(
                activeCourseId = occupationTitle,
                courses = existingCourses
            )
        }
        syncLocalDataToCloud()
    }

    override suspend fun updateUserStats(xpEarned: Int, gemsEarned: Int) {
        val todayStr = getTodayDateString()
        Log.d(TAG, "updateUserStats Triggered: Earned XP=$xpEarned, Gems=$gemsEarned")

        preferencesDataSource.updateData { current ->
            var activeId = current.activeCourseId
            if (activeId == null && current.courses.isNotEmpty()) {
                activeId = current.courses.keys.first()
            }

            if (activeId == null) {
                Log.e(TAG, "XP Update Failed: No active course found!")
                return@updateData current
            }

            val activeProgress = current.courses[activeId] ?: CourseProgress(level = 1, xp = 0)

            val newCourseXp = activeProgress.xp + xpEarned
            val updatedProgress = activeProgress.copy(xp = newCourseXp)

            val updatedCourses = current.courses.toMutableMap()
            updatedCourses[activeId] = updatedProgress

            var newStreak = current.streak
            var newHasStreakFreeze = current.hasStreakFreeze
            val lastLessonDate = current.lastLessonDate

            if (lastLessonDate == null) {
                newStreak = 1
            } else if (isToday(lastLessonDate)) {
                newStreak = current.streak
            } else if (isYesterday(lastLessonDate)) {
                newStreak = current.streak + 1
            } else {
                if (current.hasStreakFreeze) {
                    newHasStreakFreeze = false
                } else {
                    newStreak = 1
                }
            }

            val correctTotalXp = updatedCourses.values.sumOf { it.xp }
            Log.d(TAG, "XP Updated Locally: OldTotal=${current.totalXp}, NewTotal=$correctTotalXp")

            current.copy(
                gems = current.gems + gemsEarned,
                courses = updatedCourses,
                streak = newStreak,
                hasStreakFreeze = newHasStreakFreeze,
                lastLessonDate = todayStr,
                totalXp = correctTotalXp,
                activeCourseId = activeId
            )
        }

        syncLocalDataToCloud()
    }

    override suspend fun deductHeart() {
        preferencesDataSource.updateData { current ->
            current.copy(hearts = (current.hearts - 1).coerceAtLeast(0))
        }
        syncLocalDataToCloud()
    }

    override suspend fun unlockNextLevel() {
        preferencesDataSource.updateData { current ->
            val activeId = current.activeCourseId ?: return@updateData current
            val activeProgress = current.courses[activeId] ?: CourseProgress()

            val currentStage = activeProgress.stagesCompleted
            val currentLevel = activeProgress.level
            val stagesRequired = 3

            val updatedProgress = if (currentStage + 1 >= stagesRequired) {
                activeProgress.copy(level = currentLevel + 1, stagesCompleted = 0, isFirstLessonDone = true)
            } else {
                activeProgress.copy(stagesCompleted = currentStage + 1, isFirstLessonDone = true)
            }

            val updatedCourses = current.courses.toMutableMap()
            updatedCourses[activeId] = updatedProgress
            current.copy(courses = updatedCourses)
        }
        syncLocalDataToCloud()
    }

    // --- Shop Logic ---

    override suspend fun buyHeartRefill(): Result<Unit> {
        val cost = 50
        return try {
            val currentData = preferencesDataSource.userData.first()
            if (currentData.hearts >= 5) return Result.failure(Exception("Hearts are already full!"))
            if (currentData.gems < cost) return Result.failure(Exception("Not enough gems!"))
            preferencesDataSource.updateData { it.copy(gems = it.gems - cost, hearts = 5) }
            syncLocalDataToCloud()
            Result.success(Unit)
        } catch (e: Exception) { Result.failure(e) }
    }

    override suspend fun buyStreakFreeze(): Result<Unit> {
        val cost = 200
        return try {
            val currentData = preferencesDataSource.userData.first()
            if (currentData.hasStreakFreeze) return Result.failure(Exception("You already have a Streak Freeze!"))
            if (currentData.gems < cost) return Result.failure(Exception("Not enough gems!"))
            preferencesDataSource.updateData { it.copy(gems = it.gems - cost, hasStreakFreeze = true) }
            syncLocalDataToCloud()
            Result.success(Unit)
        } catch (e: Exception) { Result.failure(e) }
    }

    // --- Categories Logic (GÜNCELLENDİ) ---

    override suspend fun markCategoryAsCompleted(levelId: Int, category: String) {
        // 1. Yerel veriyi güncelle
        preferencesDataSource.updateData { currentPrefs ->
            val currentMap = currentPrefs.completedCategories.toMutableMap()
            val currentLevelSet = currentMap[levelId]?.toMutableSet() ?: mutableSetOf()

            currentLevelSet.add(category)
            currentMap[levelId] = currentLevelSet

            Log.d(TAG, "Lesson Completed Locally: Level $levelId - $category")
            currentPrefs.copy(completedCategories = currentMap)
        }

        // 2. Buluta (Supabase) senkronize et
        if (authRepository.isUserLoggedIn()) {
            scope.launch {
                try {
                    val userId = authRepository.getCurrentUserId() ?: return@launch
                    val updatedLocal = preferencesDataSource.userData.first()

                    val categoriesForCloud = updatedLocal.completedCategories.entries.associate {
                        it.key.toString() to it.value.toList()
                    }

                    supabaseClient.from("profiles").update(
                        {
                            set("completed_categories", categoriesForCloud)
                        }
                    ) {
                        filter { eq("id", userId) }
                    }
                    Log.d(TAG, "Lesson Completion Synced to Cloud!")
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to sync lesson completion: ${e.message}")
                }
            }
        }
    }

    // --- Cloud Sync & Restore ---

    override suspend fun syncLocalDataToCloud() {
        if (authRepository.isUserLoggedIn()) {
            scope.launch {
                try {
                    val local = preferencesDataSource.userData.first()
                    val userId = authRepository.getCurrentUserId() ?: return@launch

                    Log.d(TAG, "Syncing to Cloud... XP: ${local.totalXp}, Gems: ${local.gems}")

                    val categoriesForCloud = local.completedCategories.entries.associate {
                        it.key.toString() to it.value.toList()
                    }

                    val updateData = RemoteProfileDto(
                        id = userId,
                        gems = local.gems,
                        hearts = local.hearts,
                        streak = local.streak,
                        hasStreakFreeze = local.hasStreakFreeze,
                        totalXp = local.totalXp,
                        lastLessonDate = local.lastLessonDate,
                        completedCategories = categoriesForCloud,
                        username = local.username?.ifBlank { null },
                        avatarUrl = local.avatarUrl,
                        activeCourseId = local.activeCourseId,
                        courses = local.courses,
                        experienceLevel = local.experienceLevel
                    )

                    supabaseClient.from("profiles").upsert(updateData) {
                        filter { eq("id", userId) }
                    }
                    Log.d(TAG, "Sync Successful!")
                } catch (e: Exception) {
                    Log.e(TAG, "Sync Failed: ${e.message}")
                }
            }
        }
    }

    override suspend fun restoreFromCloud() {
        if (authRepository.isUserLoggedIn()) {
            val userId = authRepository.getCurrentUserId() ?: throw Exception("Auth token missing")
            try {
                val remoteProfile = supabaseClient.from("profiles")
                    .select { filter { eq("id", userId) } }
                    .decodeSingleOrNull<RemoteProfileDto>()

                if (remoteProfile != null) {
                    Log.d(TAG, "Restored from Cloud. Cloud XP: ${remoteProfile.totalXp}, Name: ${remoteProfile.username}")

                    val localCategories = remoteProfile.completedCategories.entries.associate {
                        it.key.toInt() to it.value.toSet()
                    }

                    preferencesDataSource.updateData { current ->
                        current.copy(
                            gems = remoteProfile.gems,
                            hearts = remoteProfile.hearts,
                            streak = remoteProfile.streak,
                            hasStreakFreeze = remoteProfile.hasStreakFreeze,
                            totalXp = remoteProfile.totalXp,
                            lastLessonDate = remoteProfile.lastLessonDate,
                            completedCategories = localCategories,
                            userId = userId,
                            username = remoteProfile.username ?: current.username,
                            avatarUrl = remoteProfile.avatarUrl ?: current.avatarUrl,
                            activeCourseId = remoteProfile.activeCourseId,
                            courses = remoteProfile.courses,
                            experienceLevel = remoteProfile.experienceLevel ?: current.experienceLevel
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Restore Failed: ${e.message}")
                throw e
            }
        }
    }

    // --- Settings & Helpers ---

    override suspend fun updateSettings(isVibration: Boolean, isDarkMode: Boolean, isNotifications: Boolean) {
        val userId = supabaseClient.auth.currentUserOrNull()?.id
        preferencesDataSource.updateData {
            it.copy(
                isVibrationEnabled = isVibration,
                isDarkModeEnabled = isDarkMode,
                isNotificationsEnabled = isNotifications
            )
        }
        if (userId != null) {
            scope.launch {
                try {
                    supabaseClient.from("profiles").update({
                        set("is_vibration_enabled", isVibration)
                        set("is_dark_mode_enabled", isDarkMode)
                        set("is_notifications_enabled", isNotifications)
                    }) { filter { eq("id", userId) } }
                } catch (e: Exception) {
                    Log.e(TAG, "Settings Sync Failed: ${e.message}")
                }
            }
        }
    }

    override suspend fun clearLocalData() {
        preferencesDataSource.clearData()
    }

    private fun getTodayDateString(): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            LocalDate.now().toString()
        } else {
            java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
        }
    }

    private fun isYesterday(lastDateStr: String): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                val lastDate = LocalDate.parse(lastDateStr)
                val today = LocalDate.now()
                return ChronoUnit.DAYS.between(lastDate, today) == 1L
            } catch (_: Exception) { return false }
        }
        return false
    }

    private fun isToday(lastDateStr: String): Boolean {
        return lastDateStr == getTodayDateString()
    }
}

@Serializable
private data class RemoteProfileDto(
    val id: String,
    val gems: Int = 0,
    val hearts: Int = 5,
    val streak: Int = 0,
    @SerialName("has_streak_freeze") val hasStreakFreeze: Boolean = false,

    @SerialName("xp") val totalXp: Int = 0,

    @SerialName("last_lesson_date") val lastLessonDate: String? = null,
    @SerialName("completed_categories") val completedCategories: Map<String, List<String>> = emptyMap(),

    @SerialName("username") val username: String? = null,
    @SerialName("avatar_url") val avatarUrl: String? = null,

    @SerialName("active_course_id") val activeCourseId: String? = null,
    @SerialName("courses") val courses: Map<String, CourseProgress> = emptyMap(),
    @SerialName("experience_level") val experienceLevel: String? = null
)