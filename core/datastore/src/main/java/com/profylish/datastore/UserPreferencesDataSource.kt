package com.profylish.datastore

import androidx.datastore.core.DataStore
import com.profylish.model.user.UserPreferences
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class UserPreferencesDataSource @Inject constructor(
    private val userPreferences: DataStore<UserPreferences>
) {
    val userData: Flow<UserPreferences> = userPreferences.data

    suspend fun updateData(
        transform: suspend (UserPreferences) -> UserPreferences
    ) {
        userPreferences.updateData(transform)
    }

    suspend fun clearData() {
        userPreferences.updateData {
            UserPreferences()
        }
    }
}