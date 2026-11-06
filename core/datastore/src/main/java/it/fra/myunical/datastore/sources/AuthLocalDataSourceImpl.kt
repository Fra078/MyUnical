package it.fra.myunical.datastore.sources

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import it.fra.myunical.domain.model.auth.UserCredentials
import it.fra.myunical.domain.sources.AuthLocalDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map

class AuthLocalDataSourceImpl(
    private val dataStore: DataStore<Preferences>
) : AuthLocalDataSource {

    override suspend fun saveCredentials(credentials: UserCredentials){
        dataStore.edit {
            it[USERNAME_KEY] = credentials.username
            it[PASSWORD_KEY] = credentials.password
        }
    }

    override suspend fun clearCredentials(){
        dataStore.edit {
            it.remove(USERNAME_KEY)
            it.remove(PASSWORD_KEY)
        }
    }

    override suspend fun getCredentials(): UserCredentials? {
        val preferences = dataStore.data.firstOrNull() ?: return null
        val username = preferences[USERNAME_KEY] ?: return null
        val password = preferences[PASSWORD_KEY] ?: return null

        return UserCredentials(username = username, password = password)
    }

    override fun observeUsername(): Flow<String?> {
        return dataStore.data.map { it[USERNAME_KEY] }
    }

    private companion object {
        val USERNAME_KEY = stringPreferencesKey("username")
        val PASSWORD_KEY = stringPreferencesKey("password")
    }
}