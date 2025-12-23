package com.profylish.data.repository

import com.profylish.datastore.UserPreferencesDataSource
import com.profylish.domain.repository.UserDataRepository
import com.profylish.model.user.UserPreferences
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class UserDataRepositoryImpl @Inject constructor(
    private val preferencesDataSource: UserPreferencesDataSource
) : UserDataRepository {

    override val userData: Flow<UserPreferences> = preferencesDataSource.userData

    override suspend fun saveUserSelection(occupationId: String, occupationGroup: String, level: String) {
        preferencesDataSource.saveUserSelection(occupationId, occupationGroup, level)
    }
}