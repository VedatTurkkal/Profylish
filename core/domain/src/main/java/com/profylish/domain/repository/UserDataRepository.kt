package com.profylish.domain.repository

import kotlinx.coroutines.flow.Flow
import com.profylish.model.user.UserPreferences

interface UserDataRepository {
    val userData: Flow<UserPreferences>
    suspend fun saveUserSelection(occupationId: String, occupationGroup: String, level: String)
}