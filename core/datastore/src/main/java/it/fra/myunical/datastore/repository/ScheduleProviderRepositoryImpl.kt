package it.fra.myunical.datastore.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import it.fra.myunical.datastore.mapper.toDataModel
import it.fra.myunical.datastore.mapper.toDomainModel
import it.fra.myunical.datastore.models.ScheduleProviderDataModel
import it.fra.myunical.domain.model.schedule.ScheduleProvider
import it.fra.myunical.domain.repository.ScheduleProviderRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json

class ScheduleProviderRepositoryImpl(
    private val json: Json,
    private val dataStore: DataStore<Preferences>
) : ScheduleProviderRepository {

    override fun getProviders() : Flow<List<ScheduleProvider>> =
        dataStore.data
            .map { it.readProviders().map(ScheduleProviderDataModel::toDomainModel) }
            .distinctUntilChanged()

    override suspend fun getProvider(id: String): ScheduleProvider? =
        getProviders().first().find { it.id == id }

    override suspend fun upsertProvider(item: ScheduleProvider) {
        dataStore.edit { preferences ->
            val providers = preferences.readProviders()
            val dataModel = item.toDataModel()
            item.careerMatId?.let { preferences.writeDismissed(preferences.readDismissed() - it) }
            val index = providers.indexOfFirst { it.id == item.id }
            preferences.writeProviders(
                if (index == -1) providers + dataModel
                else providers.toMutableList().apply { set(index, dataModel) }
            )
        }
    }

    override suspend fun removeProvider(id: String) {
        dataStore.edit { preferences ->
            val providers = preferences.readProviders()
            providers.find { it.id == id }?.careerMatId?.let { preferences.writeDismissed(preferences.readDismissed() + it) }
            preferences.writeProviders(providers.filterNot { it.id == id })
        }
    }

    override suspend fun isCareerScheduleDismissed(careerMatId: Int): Boolean =
        careerMatId in dataStore.data.first().readDismissed()

    private fun Preferences.readDismissed(): Set<Int> =
        this[DISMISSED_KEY]?.let { json.decodeFromString<Set<Int>>(it) }.orEmpty()

    private fun MutablePreferences.writeDismissed(careers: Set<Int>) {
        this[DISMISSED_KEY] = json.encodeToString(careers)
    }

    private fun Preferences.readProviders(): List<ScheduleProviderDataModel> =
        this[PROVIDERS_KEY]?.let { json.decodeFromString<List<ScheduleProviderDataModel>>(it) }.orEmpty()

    private fun MutablePreferences.writeProviders(providers: List<ScheduleProviderDataModel>) {
        this[PROVIDERS_KEY] = json.encodeToString(providers)
    }

    private companion object {
        val PROVIDERS_KEY = stringPreferencesKey("providers")
        val DISMISSED_KEY = stringPreferencesKey("dismissed_career_schedules")
    }

}
