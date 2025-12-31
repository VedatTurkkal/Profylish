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

    // --- Profil Güncelleme ---
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
            Log.e("ProfileUpdate", "Error updating cloud: ${e.message}")
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

        // upsert true parametresi
        bucket.upload(fileName, byteArray, true)

        val publicUrl = bucket.publicUrl(fileName)

        updateProfile(username = null, avatarUrl = publicUrl)

        return publicUrl
    }

    override suspend fun updateUsername(newUsername: String) {
        updateProfile(username = newUsername, avatarUrl = null)
    }

    // --- Tarih Yardımcıları ---
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

    // --- Ders ve İstatistik İşlemleri ---
    override suspend fun switchOrAddCourse(occupationTitle: String) {
        preferencesDataSource.updateData { current ->
            val existingCourses = current.courses.toMutableMap()
            if (existingCourses.containsKey(occupationTitle)) {
                return@updateData current.copy(activeCourseId = occupationTitle)
            }
            if (existingCourses.size < 3) {
                existingCourses[occupationTitle] = CourseProgress(level = 1, xp = 0)
                return@updateData current.copy(
                    activeCourseId = occupationTitle,
                    courses = existingCourses
                )
            }
            current
        }
        syncToCloud()
    }

    override suspend fun updateUserStats(xpEarned: Int, gemsEarned: Int) {
        val todayStr = getTodayDateString()
        preferencesDataSource.updateData { current ->
            val activeId = current.activeCourseId ?: return@updateData current
            val activeProgress = current.courses[activeId] ?: CourseProgress()
            val updatedCourses = current.courses.toMutableMap()
            updatedCourses[activeId] = activeProgress.copy(xp = activeProgress.xp + xpEarned)
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
            current.copy(
                gems = current.gems + gemsEarned,
                courses = updatedCourses,
                streak = newStreak,
                hasStreakFreeze = newHasStreakFreeze,
                lastLessonDate = todayStr,
                totalXp = updatedCourses.values.sumOf { it.xp }
            )
        }
        syncToCloud()
    }

    override suspend fun deductHeart() {
        preferencesDataSource.updateData { current ->
            current.copy(hearts = (current.hearts - 1).coerceAtLeast(0))
        }
        syncToCloud()
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
        syncToCloud()
    }

    override suspend fun buyHeartRefill(): Result<Unit> {
        val cost = 50
        return try {
            val currentData = preferencesDataSource.userData.first()
            if (currentData.hearts >= 5) return Result.failure(Exception("Hearts are already full!"))
            if (currentData.gems < cost) return Result.failure(Exception("Not enough gems!"))
            preferencesDataSource.updateData { it.copy(gems = it.gems - cost, hearts = 5) }
            syncToCloud()
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
            syncToCloud()
            Result.success(Unit)
        } catch (e: Exception) { Result.failure(e) }
    }

    // --- Sync & Restore ---
    private fun syncToCloud() {
        if (authRepository.isUserLoggedIn()) {
            scope.launch {
                try {
                    val local = preferencesDataSource.userData.first()
                    val userId = authRepository.getCurrentUserId() ?: return@launch
                    val totalXpCalculated = local.courses.values.sumOf { it.xp }
                    val profileData = local.copy(userId = userId, totalXp = totalXpCalculated)
                    supabaseClient.from("profiles").upsert(profileData) { filter { eq("id", userId) } }
                } catch (e: Exception) { Log.e("SyncError", "Failed to sync: ${e.message}") }
            }
        }
    }

    override suspend fun restoreFromCloud() {
        if (authRepository.isUserLoggedIn()) {
            val userId = authRepository.getCurrentUserId() ?: throw Exception("Auth token missing")
            try {
                val remoteProfile = supabaseClient.from("profiles")
                    .select { filter { eq("id", userId) } }
                    .decodeSingleOrNull<UserPreferences>()
                if (remoteProfile != null) {
                    preferencesDataSource.updateData { current ->
                        current.copy(
                            gems = remoteProfile.gems,
                            hearts = remoteProfile.hearts,
                            streak = remoteProfile.streak,
                            hasStreakFreeze = remoteProfile.hasStreakFreeze,
                            courses = remoteProfile.courses,
                            activeCourseId = remoteProfile.activeCourseId,
                            username = remoteProfile.username,
                            avatarUrl = remoteProfile.avatarUrl,
                            experienceLevel = remoteProfile.experienceLevel,
                            lastLessonDate = remoteProfile.lastLessonDate,
                            totalXp = remoteProfile.totalXp,
                            userId = userId
                        )
                    }
                }
            } catch (e: Exception) { throw e }
        }
    }

    // --- Avatar Silme ve Ayarlar ---
    override suspend fun deleteAvatar() {
        val userId = supabaseClient.auth.currentUserOrNull()?.id ?: return

        // Local güncelleme (Hız için önce)
        preferencesDataSource.updateData { it.copy(avatarUrl = null) }

        // Cloud güncelleme
        try {
            supabaseClient.from("profiles").update(
                {
                    // [DÜZELTME] 'null as String?' kullanarak hangi set metodunun çağrılacağını belirtiyoruz.
                    set("avatar_url", null as String?)
                }
            ) { filter { eq("id", userId) } }
        } catch (e: Exception) { e.printStackTrace() }
    }

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
                } catch (e: Exception) { Log.e("Settings", "Sync failed") }
            }
        }
    }

    override suspend fun clearLocalData() { preferencesDataSource.clearData() }
    override suspend fun syncLocalDataToCloud() { syncToCloud() }
}