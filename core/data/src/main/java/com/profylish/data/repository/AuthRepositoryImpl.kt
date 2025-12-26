package com.profylish.data.repository

import com.profylish.domain.repository.AuthRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val supabaseClient: SupabaseClient
) : AuthRepository {

    // DÜZELTME 1: (session = _) kısmı silindi. Sadece tip kontrolü yeterli.
    override val authState: Flow<Boolean> = supabaseClient.auth.sessionStatus.map { status ->
        status is SessionStatus.Authenticated
    }

    // DÜZELTME 2: Parametre adı 'pass' yerine 'password' yapıldı.
    override suspend fun signUp(email: String, password: String): Result<Unit> {
        return try {
            supabaseClient.auth.signUpWith(Email) {
                this.email = email
                this.password = password // Parametre ismi düzeltildi
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // DÜZELTME 3: Parametre adı 'pass' yerine 'password' yapıldı.
    override suspend fun signIn(email: String, password: String): Result<Unit> {
        return try {
            supabaseClient.auth.signInWith(Email) {
                this.email = email
                this.password = password // Parametre ismi düzeltildi
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signOut() {
        supabaseClient.auth.signOut()
    }

    override fun isUserLoggedIn(): Boolean {
        return supabaseClient.auth.currentSessionOrNull() != null
    }

    override fun getCurrentUserId(): String? {
        return supabaseClient.auth.currentUserOrNull()?.id
    }
}