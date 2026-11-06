package it.fra.myunical.domain.usecase

import it.fra.myunical.domain.model.auth.AuthState
import it.fra.myunical.domain.sources.AuthLocalDataSource
import kotlinx.coroutines.flow.map

class GetAuthStateUseCase(
    private val localAuthDataSource: AuthLocalDataSource
) {

    operator fun invoke() = localAuthDataSource.observeUsername().map {
        if (it == null)
            AuthState.Unauthenticated
        else
            AuthState.Authenticated
    }

}