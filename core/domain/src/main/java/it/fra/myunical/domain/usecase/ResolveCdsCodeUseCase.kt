package it.fra.myunical.domain.usecase

import it.fra.myunical.domain.model.auth.AuthResponseModel
import it.fra.myunical.domain.model.general.CareerTrackModel
import it.fra.myunical.domain.sources.AuthLocalDataSource
import it.fra.myunical.domain.sources.AuthRemoteDataSource
import it.fra.myunical.domain.sources.UserInfoDataSource
import kotlinx.coroutines.CancellationException

/**
 * Degree course code of a career. Careers saved by older versions lack it, without which modules,
 * groups and regulations cannot be found: the profile is downloaded again with the saved
 * credentials and stored.
 */
class ResolveCdsCodeUseCase(
    private val authLocalDataSource: AuthLocalDataSource,
    private val authRemoteDataSource: AuthRemoteDataSource,
    private val userInfoDataSource: UserInfoDataSource
) {
    suspend operator fun invoke(career: CareerTrackModel): String? {
        career.cdsCode?.let { return it }
        val credentials = authLocalDataSource.getCredentials() ?: return null
        val response = try {
            authRemoteDataSource.validateCredentials(credentials)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            return null
        }
        if (response !is AuthResponseModel.Success) return null
        userInfoDataSource.saveUserInfo(response.content)
        return response.content.careers.firstOrNull { it.matId == career.matId }?.cdsCode
    }
}
