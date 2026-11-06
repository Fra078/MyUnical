package it.fra.myunical.network.sources

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpStatusCode
import it.fra.myunical.domain.model.career.CareerOverview
import it.fra.myunical.domain.model.career.ExamBooking
import it.fra.myunical.domain.model.career.ExamSession
import it.fra.myunical.domain.model.general.Course
import it.fra.myunical.domain.sources.CinecaRemoteDataSource
import it.fra.myunical.network.di.Endpoints
import it.fra.myunical.network.mapper.toCareerOverview
import it.fra.myunical.network.mapper.toDomainModel
import it.fra.myunical.network.models.cineca.ExamBookingDto
import it.fra.myunical.network.models.cineca.ExamSessionDto
import it.fra.myunical.network.models.cineca.LibrettoAverageDto
import it.fra.myunical.network.models.cineca.LibrettoStatsDto
import it.fra.myunical.network.models.cineca.StudyPlanItemDto
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

class CinecaRemoteDataSourceImpl(
    private val httpClient: HttpClient
) : CinecaRemoteDataSource {

    override fun getUserPhotoEndpoint(persId: Int): String =
        "${Endpoints.ESSE3}anagrafica-service-v2/persone/$persId/foto"

    override suspend fun fetchStudyPlan(matId: Int): Result<List<Course>> = try {
        Result.success(
            getOk("libretto-service-v2/libretti/$matId/righe").body<List<StudyPlanItemDto>>()
                .map(StudyPlanItemDto::toDomainModel)
        )
    } catch (e: CancellationException){
        throw e
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun fetchCareerOverview(matId: Int): CareerOverview = coroutineScope {
        val averages = async { getOk("libretto-service-v2/libretti/$matId/medie").body<List<LibrettoAverageDto>>() }
        val stats = getOk("libretto-service-v2/libretti/$matId/stats").body<LibrettoStatsDto>()
        stats.toCareerOverview(averages.await())
    }

    override suspend fun fetchExamSessions(matId: Int): List<ExamSession> =
        getOk("libretto-service-v2/libretti/$matId/appelli").body<List<ExamSessionDto>>()
            .mapNotNull(ExamSessionDto::toDomainModel)

    override suspend fun fetchBookings(matId: Int): List<ExamBooking> =
        getOk("libretto-service-v2/libretti/$matId/prenotazioni").body<List<ExamBookingDto>>()
            .mapNotNull(ExamBookingDto::toDomainModel)

    private suspend fun getOk(path: String): HttpResponse {
        val response = httpClient.get(path)
        if (response.status != HttpStatusCode.OK)
            throw IllegalStateException("Errore nella richiesta: ${response.status}")
        return response
    }
}
