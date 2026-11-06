package it.fra.myunical.network.sources

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode
import it.fra.myunical.domain.model.general.Course
import it.fra.myunical.domain.sources.CinecaRemoteDataSource
import it.fra.myunical.network.di.BaseUrlKey
import it.fra.myunical.network.mapper.toDomainModel
import it.fra.myunical.network.models.cineca.StudyPlanItemDto
import kotlinx.coroutines.CancellationException

class CinecaRemoteDataSourceImpl(
    private val httpClient: HttpClient
) : CinecaRemoteDataSource {

    override fun getUserPhotoEndpoint(persId: Int) : String {
        val baseUrl = httpClient.attributes.getOrNull(BaseUrlKey)
        return baseUrl + "anagrafica-service-v2/persone/$persId/foto"
    }

    override suspend fun fetchStudyPlan(matId: Int): Result<List<Course>> = try {
        val response = httpClient.get("libretto-service-v2/libretti/$matId/righe")
        if (response.status != HttpStatusCode.OK)
            Result.failure(Exception("Errore nella richiesta: ${response.status}"))
        else
            Result.success(
                response.body<List<StudyPlanItemDto>>()
                    .map(StudyPlanItemDto::toDomainModel)
            )
    } catch (e: CancellationException){
        throw e
    } catch (e: Exception) {
        Result.failure(e)
    }

}