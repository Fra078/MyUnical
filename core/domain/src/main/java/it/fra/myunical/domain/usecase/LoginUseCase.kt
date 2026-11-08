package it.fra.myunical.domain.usecase

import it.fra.myunical.domain.model.auth.AuthError
import it.fra.myunical.domain.model.auth.AuthResponseModel
import it.fra.myunical.domain.model.auth.UserCredentials
import it.fra.myunical.domain.sources.AuthRemoteDataSource
import it.fra.myunical.domain.sources.UserInfoDataSource

class LoginUseCase(
    private val userInfoDataSource: UserInfoDataSource,
    private val remoteAuthDataSource: AuthRemoteDataSource
) {

    suspend operator fun invoke(credentials: UserCredentials) : AuthResponseModel {
        val cleanCredentials = credentials.sanitized()
        if (cleanCredentials.username.isEmpty() || cleanCredentials.password.isEmpty())
            return AuthResponseModel.Failure(AuthError.InvalidCredentials)
        val response = remoteAuthDataSource.validateCredentials(cleanCredentials)
        if (response is AuthResponseModel.Success){
            userInfoDataSource.saveUserInfo(response.content)
        }
        return response
    }

}