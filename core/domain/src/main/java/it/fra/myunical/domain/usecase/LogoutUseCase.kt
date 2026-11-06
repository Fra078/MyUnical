package it.fra.myunical.domain.usecase

import it.fra.myunical.domain.sources.AuthLocalDataSource

/** Forgets the saved credentials; the app observes them and goes back to the login screen. */
class LogoutUseCase(
    private val authLocalDataSource: AuthLocalDataSource
) {
    suspend operator fun invoke() = authLocalDataSource.clearCredentials()
}
