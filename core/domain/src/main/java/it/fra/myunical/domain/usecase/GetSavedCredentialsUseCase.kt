package it.fra.myunical.domain.usecase

import it.fra.myunical.domain.model.auth.UserCredentials
import it.fra.myunical.domain.sources.AuthLocalDataSource

/** The saved Esse3 credentials, or `null` when logged out; used to sign in to the Esse3 website. */
class GetSavedCredentialsUseCase(
    private val authLocalDataSource: AuthLocalDataSource
) {
    suspend operator fun invoke(): UserCredentials? = authLocalDataSource.getCredentials()
}
