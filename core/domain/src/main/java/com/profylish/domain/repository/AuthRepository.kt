package com.profylish.domain.repository

import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    suspend fun signUp(email: String, password: String): Result<Unit>
    suspend fun signIn(email: String, password: String): Result<Unit>
    suspend fun signOut()
    fun isUserLoggedIn(): Boolean
    fun getCurrentUserId(): String?
    val authState: Flow<Boolean>
}