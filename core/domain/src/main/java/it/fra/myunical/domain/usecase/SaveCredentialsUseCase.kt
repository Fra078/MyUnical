package it.fra.myunical.domain.usecase

import it.fra.myunical.domain.model.auth.UserCredentials
import it.fra.myunical.domain.repository.UserPreferenceRepository
import it.fra.myunical.domain.sources.AuthLocalDataSource

class SaveCredentialsUseCase(
    private val localAuthDataSource: AuthLocalDataSource,
    private val userPreferenceRepository: UserPreferenceRepository
) {

    suspend operator fun invoke(userCredentials: UserCredentials, careerId: Int){
        localAuthDataSource.saveCredentials(userCredentials)
        userPreferenceRepository.setCareerId(careerId)
    }

}