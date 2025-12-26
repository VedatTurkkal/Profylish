package com.profylish.datastore

import androidx.datastore.core.Serializer
import com.profylish.model.user.UserPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream
import javax.inject.Inject

class UserPreferencesSerializer @Inject constructor() : Serializer<UserPreferences> {

    override val defaultValue: UserPreferences
        get() = UserPreferences()

    // JSON yapılandırmasını buraya ekliyoruz
    private val json = Json {
        ignoreUnknownKeys = true // <-- KRİTİK AYAR: Eski veya bilinmeyen alanları takma
        encodeDefaults = true
        coerceInputValues = true
    }

    override suspend fun readFrom(input: InputStream): UserPreferences {
        return try {
            json.decodeFromString(
                UserPreferences.serializer(),
                input.readBytes().decodeToString()
            )
        } catch (e: SerializationException) {
            e.printStackTrace()
            defaultValue
        }
    }

    override suspend fun writeTo(t: UserPreferences, output: OutputStream) {
        withContext(Dispatchers.IO) {
            output.write(
                json.encodeToString(
                    UserPreferences.serializer(),
                    t
                ).toByteArray()
            )
        }
    }
}