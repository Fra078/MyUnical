package it.fra.myunical.domain.usecase

import it.fra.myunical.domain.repository.UserPreferenceRepository
import kotlinx.coroutines.flow.combine

class GetCurrentCareerUseCase(
    private val userPreferenceRepository: UserPreferenceRepository,
    private val getCurrentUserInfo: GetCurrentUserInfoUseCase
) {

    operator fun invoke() =
        combine(getCurrentUserInfo(), userPreferenceRepository.getCareerId()){ userInfo, careerId ->
            userInfo.careers.find { it.matId == careerId }
        }

}