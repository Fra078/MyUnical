package it.appm.myunical.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import it.appm.myunical.data.cineca.CinecaClient
import it.appm.myunical.data.news.NewsClient
import it.appm.myunical.data.room.AppDatabase
import it.appm.myunical.data.store.AppDataStore
import it.appm.myunical.data.unistorage.UniStorageClient
import it.appm.myunical.model.InfoAppelloEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.File

class DataRepository(
    private val scope: CoroutineScope,
    private val cinecaClient: CinecaClient,
    private val database: AppDatabase,
    private val cardSource: DataStore<Preferences>,
    private val filesDir: String
) {

    private val appDataStore: AppDataStore = AppDataStore(scope, cardSource)
    private val newsClient = NewsClient()
    private val unistorageClient = UniStorageClient()

    val carriera = appDataStore.carriera
    val userPhoto = appDataStore.userPhoto
    fun getNewsDao() = database.getNewsDao()
    fun getLibretto() = database.getLibrettoDao()
    fun getSchedule(year: Int, month: Int) = database.getScheduleDao()
        .getEventsByMonthAndYearOrdered(month, year)
    fun getInfoAppelli() = database.getInfoAppelloDao()

    suspend fun aggiornaLibretto(){
        val matId = carriera.filterNotNull().first().matId
        val response = cinecaClient.getLibretto(matId)
        val entities = response.getOrNull() ?: return
        database.getLibrettoDao().insertAll(entities)
    }

    suspend fun aggiornaInfoAppelli(){
        val carr = carriera.filterNotNull().first()
        val appelli = cinecaClient.getEsamiDisponibili(carr.matId, carr.cdsId) ?: return
        database.getInfoAppelloDao().deleteAll()
        getInfoAppelli().insertAll(appelli.map { InfoAppelloEntity(it) })
    }

    fun aggiornaNews(){
        scope.launch {
            val response = newsClient.getNews()
            val news = response.getOrNull() ?: return@launch
            database.getNewsDao().clear()
            database.getNewsDao().insert(news)
        }
    }

    suspend fun aggiornaSchedule(year: Int, month: Int){
        val info = carriera.filterNotNull().first()
        val cdsCod = info.cdsCod
        val annoCds = info.annoCds
        val annoAccademico = info.annoAccademico

        val response = unistorageClient.getSchedule(cdsCod, annoAccademico, annoCds, year, month)
        val events = response.getOrNull() ?: return
        database.getScheduleDao().deleteEventsByMonthAndYear(month, year)
        database.getScheduleDao().insert(events)
    }

    suspend fun aggiornaCarriera(){
        val carriera = cinecaClient.getCarriera().getOrNull() ?: return
        appDataStore.updateCarriera(carriera)
        val photoFile = File(filesDir, "${carriera.matricola}.jpg")
        if (userPhoto.value != photoFile.absolutePath)
            scope.launch {
                val result = cinecaClient.downloadUserPhoto(carriera.persId, photoFile)
                if (result.isSuccess) appDataStore.updatePhoto(photoFile.absolutePath)
            }
    }

}