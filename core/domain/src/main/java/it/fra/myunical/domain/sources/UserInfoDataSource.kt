package it.fra.myunical.domain.sources

import it.fra.myunical.domain.model.general.UserInfoModel
import kotlinx.coroutines.flow.Flow

interface UserInfoDataSource {
    suspend fun saveUserInfo(userInfo: UserInfoModel)
    fun getUserInfo(username: String): Flow<UserInfoModel>
}