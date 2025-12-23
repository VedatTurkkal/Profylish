package com.profylish.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton
import com.profylish.model.user.UserPreferences

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

@Singleton
class UserPreferencesDataSource @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val OCCUPATION_ID_KEY = stringPreferencesKey("occupation_id") // Meslek Adı (örn: Software Dev)
    private val OCCUPATION_GROUP_KEY = stringPreferencesKey("occupation_group") // ✅ YENİ: Grup Adı (örn: Computer Ops)
    private val LEVEL_KEY = stringPreferencesKey("user_level")

    val userData: Flow<UserPreferences> = context.dataStore.data.map { prefs ->
        UserPreferences(
            occupationId = prefs[OCCUPATION_ID_KEY],
            occupationGroup = prefs[OCCUPATION_GROUP_KEY], // ✅ Okurken al
            level = prefs[LEVEL_KEY]
        )
    }

    // Kaydederken hem ID hem GROUP istiyoruz artık
    suspend fun saveUserSelection(occupationId: String, occupationGroup: String, level: String) {
        context.dataStore.edit { prefs ->
            prefs[OCCUPATION_ID_KEY] = occupationId
            prefs[OCCUPATION_GROUP_KEY] = occupationGroup // ✅ Kaydet
            prefs[LEVEL_KEY] = level
        }
    }
}