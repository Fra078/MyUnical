package it.fra.myunical.domain.usecase

import it.fra.myunical.domain.sources.AuthLocalDataSource
import it.fra.myunical.domain.sources.CinecaRemoteDataSource
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull

class GetCurrentStudyPlanUseCase(
    private val getCurrentCareerUseCase: GetCurrentCareerUseCase,
    private val cinecaDataSource: CinecaRemoteDataSource
) {

    operator fun invoke() = getCurrentCareerUseCase()
        .filterNotNull()
        .mapNotNull {
            val result = cinecaDataSource.fetchStudyPlan(it.matId)
            result.exceptionOrNull()?.printStackTrace()
            result.getOrNull()
        }
}