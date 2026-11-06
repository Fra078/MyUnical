package it.fra.myunical.datastore.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import it.fra.myunical.domain.repository.UserPreferenceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class UserPreferencesRepositoryImpl(
    private val dataStore: DataStore<Preferences>
) : UserPreferenceRepository {

    override suspend fun setCareerId(value: Int) {
        dataStore.edit { it[CAREER_ID_KEY] = value  }
    }
    override fun getCareerId(): Flow<Int> = dataStore.data.map { it[CAREER_ID_KEY] ?: 0 }

    private companion object {
        val CAREER_ID_KEY = intPreferencesKey("career_id")
    }

}