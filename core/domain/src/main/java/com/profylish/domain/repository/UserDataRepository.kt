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
    suspend fun saveUserSelection(occupationId: String, occupationGroup: String, level: String)
}