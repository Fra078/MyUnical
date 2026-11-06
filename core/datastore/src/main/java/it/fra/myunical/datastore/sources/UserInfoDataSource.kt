package it.fra.myunical.datastore.sources

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import it.fra.myunical.datastore.mapper.toDataModel
import it.fra.myunical.datastore.mapper.toDomainModel
import it.fra.myunical.datastore.models.UserInfoDataModel
import it.fra.myunical.domain.model.general.UserInfoModel
import it.fra.myunical.domain.sources.UserInfoDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.serialization.json.Json

class UserInfoDataSourceImpl(
    private val json: Json,
    private val dataStore: DataStore<Preferences>
) : UserInfoDataSource {

    override suspend fun saveUserInfo(userInfo: UserInfoModel){
        val dataModel = userInfo.toDataModel()
        dataStore.edit {
            it[stringPreferencesKey(userInfo.username)] = json.encodeToString(dataModel)
        }
    }

    override fun getUserInfo(username: String): Flow<UserInfoModel> =
        dataStore.data.mapNotNull { it[stringPreferencesKey(username)] }
            .map { json.decodeFromString<UserInfoDataModel>(it) }
            .map { it.toDomainModel() }

}