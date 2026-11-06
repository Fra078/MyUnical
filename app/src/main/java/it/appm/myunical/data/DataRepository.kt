package it.appm.myunical.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import it.appm.myunical.data.room.AppDatabase
import it.appm.myunical.data.store.AppDataStore
import it.appm.myunical.data.unistorage.UniStorageClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first

class DataRepository(
    private val scope: CoroutineScope,
    private val database: AppDatabase,
    private val cardSource: DataStore<Preferences>
) {

    private val appDataStore: AppDataStore = AppDataStore(scope, cardSource)
    private val unistorageClient = UniStorageClient()

    val carriera = appDataStore.carriera
    val userPhoto = appDataStore.userPhoto
    fun getSchedule(year: Int, month: Int) = database.getScheduleDao()
        .getEventsByMonthAndYearOrdered(month, year)

    suspend fun aggiornaSchedule(year: Int, month: Int) {
        val info = carriera.filterNotNull().first()
        val cdsCod = info.cdsCod
        val annoCds = info.annoCds
        val annoAccademico = info.annoAccademico

        val response = unistorageClient.getSchedule(cdsCod, annoAccademico, annoCds, year, month)
        val events = response.getOrNull() ?: return
        database.getScheduleDao().deleteEventsByMonthAndYear(month, year)
        database.getScheduleDao().insert(events)
    }

}