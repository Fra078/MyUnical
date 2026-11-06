package it.fra.myunical.domain.sources

import it.fra.myunical.domain.model.auth.UserCredentials
import kotlinx.coroutines.flow.Flow

interface AuthLocalDataSource {
    suspend fun saveCredentials(credentials: UserCredentials)
    suspend fun clearCredentials()
    suspend fun getCredentials(): UserCredentials?
    fun observeUsername(): Flow<String?>

}