package it.fra.myunical.domain.usecase

import it.fra.myunical.domain.repository.UserPreferenceRepository

class SetCurrentCareerUseCase(
    private val userPreferenceRepository: UserPreferenceRepository
) {
    suspend operator fun invoke(matId: Int) = userPreferenceRepository.setCareerId(matId)
}
