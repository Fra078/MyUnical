package it.fra.myunical.datastore.di

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.preferencesDataStoreFile
import it.fra.myunical.datastore.repository.UserPreferencesRepositoryImpl
import it.fra.myunical.datastore.sources.AuthLocalDataSourceImpl
import it.fra.myunical.datastore.sources.UserInfoDataSourceImpl
import it.fra.myunical.domain.repository.UserPreferenceRepository
import it.fra.myunical.domain.sources.AuthLocalDataSource
import it.fra.myunical.domain.sources.UserInfoDataSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.json.Json
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val datastoreModule = module {
    single<AuthLocalDataSource> { AuthLocalDataSourceImpl(
        dataStore = PreferenceDataStoreFactory.create(
            scope = CoroutineScope(Dispatchers.IO),
            produceFile = { androidContext().preferencesDataStoreFile("auth_secure.preference") }
        )
    ) }

    single<UserPreferenceRepository> {
        UserPreferencesRepositoryImpl(
            dataStore = PreferenceDataStoreFactory.create(
                scope = CoroutineScope(Dispatchers.IO),
                produceFile = { androidContext().preferencesDataStoreFile("user.preference") }
            )
        )
    }
    single<UserInfoDataSource> {
        UserInfoDataSourceImpl(
            json = Json,
            dataStore = PreferenceDataStoreFactory.create(
                scope = CoroutineScope(Dispatchers.IO),
                produceFile = { androidContext().preferencesDataStoreFile("user_info.preference") }
            )
        )
    }
}