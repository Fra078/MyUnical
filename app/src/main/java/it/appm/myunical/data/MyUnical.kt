package it.appm.myunical.data

import android.app.Application
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.room.Room
import androidx.room.RoomDatabase
import it.appm.myunical.data.cineca.CinecaClient
import it.appm.myunical.data.cineca.LoginState
import it.appm.myunical.data.room.AppDatabase
import it.appm.myunical.data.store.CardSource
import it.appm.myunical.data.store.cardSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

object MyUnical {
    private lateinit var filesDir: String
    private lateinit var cardSource: DataStore<Preferences>
    private lateinit var db : AppDatabase
    private val scope = CoroutineScope(Dispatchers.IO)

    private val _state = MutableStateFlow(LoginState.LOADING)
    val state = _state.asStateFlow()

    lateinit var dataRepository : DataRepository

    fun login(codiceFiscale: String, password: String){
        _state.value = LoginState.LOGGING_IN
        scope.launch {
            val temporaryClient = CinecaClient.fromPassword(codiceFiscale, password)
            val response = temporaryClient.login()

            if (response == LoginState.READY) {
                cardSource.updateData { prefs->
                    prefs.toMutablePreferences().apply {
                        set(CardSource.codice_fiscale, codiceFiscale)
                        set(CardSource.token, temporaryClient.token)
                    }
                }
                dataRepository = DataRepository(scope, temporaryClient, db, cardSource, filesDir)
            }
            _state.value = response
        }
    }

    fun backToLogin(){
        _state.value = LoginState.WAIT_CREDENTIALS
    }

    private fun tryInitialSetup(){
        scope.launch(Dispatchers.Default) {
            val info = cardSource.data.first()
            if (info.contains(CardSource.codice_fiscale) && info.contains(CardSource.token)){
                if (_state.value == LoginState.LOADING){
                    val client = CinecaClient(info[CardSource.codice_fiscale]!!, info[CardSource.token]!!)
                    dataRepository = DataRepository(scope, client, db, cardSource, filesDir)
                    _state.value = LoginState.READY
                }
            } else {
                _state.value = LoginState.WAIT_CREDENTIALS
            }

        }
    }

    fun setup(application: Application){
        cardSource = application.cardSource
        filesDir = application.filesDir.absolutePath
        db = Room
            .databaseBuilder(application, AppDatabase::class.java, "app-db")
            .fallbackToDestructiveMigration()
            .build()
        tryInitialSetup()
    }
}