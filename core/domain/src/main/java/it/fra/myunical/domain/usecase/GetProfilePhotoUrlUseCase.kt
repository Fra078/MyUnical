package it.fra.myunical.domain.usecase

import it.fra.myunical.domain.sources.CinecaRemoteDataSource

/** URL of the student's Esse3 photo; the image loader adds the credentials. */
class GetProfilePhotoUrlUseCase(
    private val cinecaDataSource: CinecaRemoteDataSource
) {
    operator fun invoke(persId: Int): String = cinecaDataSource.getUserPhotoEndpoint(persId)
}
