package it.fra.myunical.domain.model.auth

sealed interface AuthError {

    data object InvalidCredentials : AuthError

    data object NetworkUnavailable: AuthError

    data class ServerError(val code: Int, val message: String?) : AuthError

    data class Unknown(val throwable: Throwable) : AuthError

}