@file:OptIn(ExperimentalCoroutinesApi::class)

package it.fra.myunical.domain.usecase

import it.fra.myunical.domain.sources.AuthLocalDataSource
import it.fra.myunical.domain.sources.UserInfoDataSource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map

class GetCurrentUserInfoUseCase(
    private val authLocalDataSource: AuthLocalDataSource,
    private val userInfoDataSource: UserInfoDataSource
) {

    operator fun invoke() = authLocalDataSource.observeUsername().filterNotNull().flatMapLatest { username ->
        userInfoDataSource.getUserInfo(username)
    }


}