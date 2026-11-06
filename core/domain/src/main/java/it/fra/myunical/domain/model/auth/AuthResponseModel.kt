package it.fra.myunical.domain.model.auth

import it.fra.myunical.domain.model.general.UserInfoModel

sealed interface AuthResponseModel {

    data class Success(val content: UserInfoModel) : AuthResponseModel

    data class Failure(val error: AuthError) : AuthResponseModel

}