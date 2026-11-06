package it.fra.myunical.domain.sources

import it.fra.myunical.domain.model.auth.AuthResponseModel
import it.fra.myunical.domain.model.auth.UserCredentials

interface AuthRemoteDataSource {

    suspend fun validateCredentials(credentials: UserCredentials) : AuthResponseModel

}