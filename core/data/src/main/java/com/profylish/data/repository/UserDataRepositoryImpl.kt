package com.profylish.data.repository

import com.profylish.datastore.UserPreferencesDataSource
import com.profylish.domain.repository.AuthRepository
import com.profylish.domain.repository.UserDataRepository
import com.profylish.model.user.CourseProgress
import com.profylish.model.user.UserPreferences
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

class UserDataRepositoryImpl @Inject constructor(
    private val preferencesDataSource: UserPreferencesDataSource,
    private val supabaseClient: SupabaseClient,
    private val authRepository: AuthRepository
) : UserDataRepository {

    override val userData: Flow<UserPreferences> = preferencesDataSource.userData
    private val scope = CoroutineScope(Dispatchers.IO)

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
        preferencesDataSource.updateData { current ->
            val activeId = current.activeCourseId ?: return@updateData current
            val activeProgress = current.courses[activeId] ?: CourseProgress()

            val updatedCourses = current.courses.toMutableMap()
            updatedCourses[activeId] = activeProgress.copy(xp = activeProgress.xp + xpEarned)

            current.copy(
                gems = current.gems + gemsEarned,
                courses = updatedCourses
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

            val updatedCourses = current.courses.toMutableMap()
            updatedCourses[activeId] = activeProgress.copy(
                level = activeProgress.level + 1,
                isFirstLessonDone = true
            )

            current.copy(courses = updatedCourses)
        }
        syncToCloud()
    }

    private fun syncToCloud() {
        if (authRepository.isUserLoggedIn()) {
            scope.launch {
                try {
                    val local = preferencesDataSource.userData.first()
                    val userId = authRepository.getCurrentUserId() ?: return@launch

                    val profileData = mapOf(
                        "id" to userId,
                        "gems" to local.gems,
                        "hearts" to local.hearts
                    )
                    supabaseClient.postgrest["profiles"].upsert(profileData)
                } catch (e: Exception) { e.printStackTrace() }
            }
        }
    }

    override suspend fun restoreFromCloud() {
    }

    override suspend fun saveUserSelection(occupationId: String, occupationGroup: String, level: String) {
    }
}