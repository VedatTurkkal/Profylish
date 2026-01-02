package com.profylish.domain.repository

import com.profylish.model.user.UserPreferences
import kotlinx.coroutines.flow.Flow

interface UserDataRepository {
    val userData: Flow<UserPreferences>

    suspend fun switchOrAddCourse(occupationTitle: String)
    suspend fun updateUserStats(xpEarned: Int, gemsEarned: Int)
    suspend fun deductHeart()
    suspend fun unlockNextLevel()

    suspend fun restoreFromCloud()

    suspend fun buyHeartRefill(): Result<Unit>
    suspend fun buyStreakFreeze(): Result<Unit>

    suspend fun updateProfile(username: String?, avatarUrl: String?)

    suspend fun updateUsername(newUsername: String)
    suspend fun clearLocalData()
    suspend fun syncLocalDataToCloud()

    suspend fun uploadAvatar(byteArray: ByteArray): String

    suspend fun deleteAvatar()
    suspend fun updateSettings(isVibration: Boolean, isDarkMode: Boolean, isNotifications: Boolean)
    suspend fun markCategoryAsCompleted(levelId: Int, category: String)
}