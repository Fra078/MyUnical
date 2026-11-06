package it.appm.myunical.data.store

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import it.appm.myunical.model.Carriera
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class AppDataStore(private val scope: CoroutineScope, private val dataSource: DataStore<Preferences>) {

    val carriera = dataSource.data.map {
        if (!it.contains(CardSource.nome)) null
        else {
            Carriera(
                nome = it[CardSource.nome]!!,
                cognome = it[CardSource.cognome]!!,
                matricola = it[CardSource.matricola]!!,
                cdsCod = it[CardSource.cdsCod]!!,
                annoAccademico = it[CardSource.annoAccademico]!!,
                facolta = it[CardSource.facolta]!!,
                matId = it[CardSource.matId]!!,
                annoCds = it[CardSource.annoCds]!!,
                persId = it[CardSource.persId]!!,
                cdsId = it[CardSource.cdsId]!!
            )
        }
    }.stateIn(scope, SharingStarted.Eagerly, null)

    val userPhoto = dataSource.data.map { it[CardSource.photoUrl] }
        .stateIn(scope, SharingStarted.Eagerly, initialValue = null)

}