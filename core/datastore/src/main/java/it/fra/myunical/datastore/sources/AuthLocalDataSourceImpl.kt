package it.fra.myunical.datastore.sources

import androidx.datastore.core.DataMigration
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import it.fra.myunical.datastore.security.CredentialCipher
import it.fra.myunical.domain.model.auth.UserCredentials
import it.fra.myunical.domain.sources.AuthLocalDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map

/** Esse3 credentials, encrypted with [cipher]: nothing readable is written to disk. */
class AuthLocalDataSourceImpl(
    private val dataStore: DataStore<Preferences>,
    private val cipher: CredentialCipher
) : AuthLocalDataSource {

    override suspend fun saveCredentials(credentials: UserCredentials) {
        dataStore.edit {
            it[USERNAME_KEY] = cipher.encrypt(credentials.username)
            it[PASSWORD_KEY] = cipher.encrypt(credentials.password)
        }
    }

    override suspend fun clearCredentials() {
        dataStore.edit {
            it.remove(USERNAME_KEY)
            it.remove(PASSWORD_KEY)
        }
    }

    override suspend fun getCredentials(): UserCredentials? {
        val preferences = dataStore.data.firstOrNull() ?: return null
        val username = preferences[USERNAME_KEY]?.let(cipher::decrypt) ?: return null
        val password = preferences[PASSWORD_KEY]?.let(cipher::decrypt) ?: return null
        return UserCredentials(username = username, password = password)
    }

    // A value that cannot be decrypted (e.g. restored from a backup on another phone) reads as signed out
    override fun observeUsername(): Flow<String?> =
        dataStore.data.map { it[USERNAME_KEY]?.let(cipher::decrypt) }.distinctUntilChanged()

    /** Encrypts the credentials that versions before encryption stored as plain text. */
    class PlaintextMigration(private val cipher: CredentialCipher) : DataMigration<Preferences> {
        override suspend fun shouldMigrate(currentData: Preferences): Boolean =
            LEGACY_USERNAME_KEY in currentData || LEGACY_PASSWORD_KEY in currentData

        override suspend fun migrate(currentData: Preferences): Preferences {
            val preferences = currentData.toMutablePreferences()
            val username = preferences.remove(LEGACY_USERNAME_KEY)
            val password = preferences.remove(LEGACY_PASSWORD_KEY)
            if (username != null && password != null) {
                preferences[USERNAME_KEY] = cipher.encrypt(username)
                preferences[PASSWORD_KEY] = cipher.encrypt(password)
            }
            return preferences.toPreferences()
        }

        override suspend fun cleanUp() = Unit
    }

    private companion object {
        val USERNAME_KEY = stringPreferencesKey("username_encrypted")
        val PASSWORD_KEY = stringPreferencesKey("password_encrypted")
        val LEGACY_USERNAME_KEY = stringPreferencesKey("username")
        val LEGACY_PASSWORD_KEY = stringPreferencesKey("password")
    }
}
