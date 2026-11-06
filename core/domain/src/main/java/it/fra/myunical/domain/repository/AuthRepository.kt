package it.fra.myunical.domain.repository

import it.fra.myunical.domain.model.auth.AuthState
import it.fra.myunical.domain.model.auth.UserCredentials
import kotlinx.coroutines.flow.StateFlow

interface AuthRepository {

    val state : StateFlow<AuthState>

    suspend fun getSavedCredentials() : UserCredentials?

    suspend fun login(credentials: UserCredentials)

    suspend fun logout()
}