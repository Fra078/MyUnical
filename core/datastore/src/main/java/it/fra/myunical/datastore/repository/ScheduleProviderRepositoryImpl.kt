package it.fra.myunical.datastore.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import it.fra.myunical.domain.model.schedule.ScheduleProvider
import it.fra.myunical.domain.repository.ScheduleProviderRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json

class ScheduleProviderRepositoryImpl(
    private val dataStore: DataStore<Preferences>
) : ScheduleProviderRepository {

    override fun getProviders() : Flow<List<ScheduleProvider>> {
        return dataStore.data
            .map { it[PROVIDERS_KEY] ?: emptySet() }
            .map { it.map(Json::decodeFromString) }
    }

    override suspend fun addProvider(item: ScheduleProvider){
        dataStore.edit {
            it[PROVIDERS_KEY] = (it[PROVIDERS_KEY] ?: emptySet()) + Json.encodeToString(item)
        }
    }
    private companion object {
        val PROVIDERS_KEY = stringSetPreferencesKey("providers")
    }

}