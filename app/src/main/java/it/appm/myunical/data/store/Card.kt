package it.appm.myunical.data.store

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore


object CardSource {
    val matId = intPreferencesKey("matId")
    val nome = stringPreferencesKey("nome")
    val cognome = stringPreferencesKey("cognome")
    val matricola = stringPreferencesKey("matricola")
    val annoCds = intPreferencesKey("annoCds")
    val annoAccademico = intPreferencesKey("annoAccademico")
    val cdsId = intPreferencesKey("cdsId")
    val cdsCod = stringPreferencesKey("cdsCod")
    val facolta = stringPreferencesKey("facolta")
    val photoUrl = stringPreferencesKey("photoUrl")
    val persId = intPreferencesKey("persId")

}